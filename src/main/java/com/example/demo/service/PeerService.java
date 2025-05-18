package com.example.demo.service;

import com.example.demo.models.dto.Peer;
import com.example.demo.models.http.PeerPageResponse;
import com.example.demo.models.http.PeerSearchRequest;
import com.example.demo.repository.CampusRepository;
import com.example.demo.repository.PeerRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PeerService {

    @PersistenceContext
    private EntityManager entityManager;

    private final PeerRepository peerRepository;
    private final CampusRepository campusRepository;
    private final PeerApiClient peerApiClient;

    @Value("${peer.page.size}")
    private static int MAX_SIZE_PAGES;
    @Value("${peer.max.pages}")
    private static int MAX_COUNT_PAGES;

    @Scheduled(cron = "0 0 0 * * *")
    public void runDailyTask() {
        updateAllPeers();
    }

    public void updateAllPeers() {
        log.info("Начато обновление пиров...");
        campusRepository.findAll().forEach(campus -> updatePeersByCampus(campus.getId()));
        log.info("Обновление завершено.");
    }

    private void updatePeersByCampus(String campusId) {
        for (int page = 0; page < MAX_COUNT_PAGES; page++) {
            int offset = page * MAX_SIZE_PAGES;
            List<String> names;

            try {
                names = peerApiClient.fetchPeerNames(campusId, MAX_SIZE_PAGES, offset).block();
            } catch (Exception e) {
                log.error("Не удалось получить имена пиров кампуса {}: {}", campusId, e.getMessage(), e);
                break;
            }

            if (names == null || names.isEmpty()) break;

            List<Peer> updatedPeers = names.parallelStream()
                    .map(this::updateSinglePeer)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            peerRepository.saveAll(updatedPeers);
            log.info("Кампус {}: обновлено {} пиров из {} со страницы {}", campusRepository.findById(campusId).get().getShortName(), updatedPeers.size(), names.size(), page);
        }
    }

    private Peer updateSinglePeer(String login) {
        try {
            Peer remotePeer = peerApiClient.fetchPeerByLogin(login).block();
            if (remotePeer == null) return null;

            Peer localPeer = peerRepository.findByLogin(login).orElse(new Peer(login));

            boolean changed = applyChangesIfNeeded(localPeer, remotePeer);
            return changed ? localPeer : null;
        } catch (Exception e) {
            log.warn("Не удалось обновить пира {}: {}", login, e.getMessage());
            return null;
        }
    }

    private boolean applyChangesIfNeeded(Peer target, Peer source) {
        boolean changed = false;

        if (!Objects.equals(target.getLevel(), source.getLevel())) {
            log.info("Пир {} изменил уровень с {} на {}", target.getLogin(), target.getLevel(), source.getLevel());
            target.setLevel(source.getLevel());
            changed = true;
        }
        if (!Objects.equals(target.getClassName(), source.getClassName())) {
            log.info("Пир {} изменил класс с {} на {}", target.getLogin(), target.getClassName(), source.getClassName());
            target.setClassName(source.getClassName());
            changed = true;
        }
        if (!Objects.equals(target.getExpValue(), source.getExpValue())) {
            log.info("Пир {} изменил expValue с {} на {}", target.getLogin(), target.getExpValue(), source.getExpValue());
            target.setExpValue(source.getExpValue());
            changed = true;
        }
        if (!Objects.equals(target.getStatus(), source.getStatus())) {
            log.info("Пир {} изменил status с {} на {}", target.getLogin(), target.getStatus(), source.getStatus());
            target.setStatus(source.getStatus());
            changed = true;
        }
        if (!Objects.equals(target.getCampus(), source.getCampus())) {
            log.info("Пир {} изменил campus с {} на {}", target.getLogin(), target.getCampus(), source.getCampus());
            target.setCampus(source.getCampus());
            changed = true;
        }
        return changed;
    }


    public PeerPageResponse searchPeers(PeerSearchRequest request) {
        log.info("Поиск пиров по запросу: {}", request);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // ===== Основной запрос =====
        CriteriaQuery<Peer> query = cb.createQuery(Peer.class);
        Root<Peer> root = query.from(Peer.class);
        List<Predicate> predicates = buildPredicates(request, cb, root);

        query.select(root)
                .where(cb.and(predicates.toArray(new Predicate[0])))
                .orderBy(cb.asc(root.get("login")));

        int page = request.getPage();
        int size = request.getSize();

        List<Peer> peers = entityManager.createQuery(query)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

        // ===== Подсчет общего количества =====
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Peer> countRoot = countQuery.from(Peer.class);
        List<Predicate> countPredicates = buildPredicates(request, cb, countRoot);

        countQuery.select(cb.count(countRoot))
                .where(cb.and(countPredicates.toArray(new Predicate[0])));

        long total = entityManager.createQuery(countQuery).getSingleResult();

        log.info("Найдено {} записей", total);
        return new PeerPageResponse(total, size, peers);
    }

    private List<Predicate> buildPredicates(PeerSearchRequest request, CriteriaBuilder cb, Root<Peer> root) {
        List<Predicate> predicates = new ArrayList<>();

        if (request.getCampusId() != null && !request.getCampusId().isEmpty()) {
            predicates.add(cb.equal(root.get("campus").get("id"), request.getCampusId()));
        }
        if (request.getStatuses() != null && !request.getStatuses().isEmpty()) {
            predicates.add(root.get("status").in(request.getStatuses()));
        }
        if (request.getPeerName() != null && !request.getPeerName().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("login")), "%" + request.getPeerName().toLowerCase() + "%"));
        }

        return predicates;
    }


    public Long countPeers(PeerSearchRequest request) {
        return searchPeers(request).getTotal();
    }
}
