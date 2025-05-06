package com.example.demo.service;

import com.example.demo.models.dto.Peer;
import com.example.demo.models.http.PeerPageResponse;
import com.example.demo.models.http.PeerSearchRequest;
import com.example.demo.models.dto.Status;

import com.example.demo.repository.CampusRepository;
import com.example.demo.repository.PeerRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PeerService {
    private final PeerRepository peerRepository;
    private final CampusRepository campusRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final AuthService authService;
    private final WebClient webClient;

    @Autowired
    public  PeerService(PeerRepository peerRepository, CampusRepository campusRepository, AuthService authService) {
        this.peerRepository = peerRepository;
        this.campusRepository = campusRepository;
        this.authService = authService;
        this.webClient  = WebClient.builder()
                .baseUrl("https://edu-api.21-school.ru/services/21-school/api/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + authService.getAccessToken())
                .build();
    }

    public void updatePeers() {
        campusRepository.findAll().forEach(campus -> {
            for (int page = 0; page < 1000; page++) {
                try {
                    List<String> peerNames = getPeersName(campus.getId(), 100, page).block();
                    if (peerNames == null || peerNames.isEmpty()) {
                        break;
                    }

                    List<Peer> peersToUpdate = peerNames.parallelStream()
                            .map(name -> peerRepository.findByLogin(name).orElseGet(() -> new Peer(name)))
                            .filter(this::updateInfo)
                            .collect(Collectors.toList());

                    peerRepository.saveAll(peersToUpdate);
                    log.info("Обновлены {} пиров кампуса", peersToUpdate.size());

                } catch (Exception e) {
                    log.error("Ошибка при обновлении пиров кампуса {}: {}", campus.getShortName(), e.getMessage(), e);
                }
            }
        });
    }

    private Boolean updateInfo(Peer peer) {
        if (peer == null) return false;
        try {
            Peer newPeer = getPeerByNameAsync(peer.getLogin()).block();
            if (peer.equals(newPeer)) return false;
            peer.setParallelName(newPeer.getParallelName());
            peer.setClassName(newPeer.getClassName());
            peer.setExpValue(newPeer.getExpValue());
            peer.setLevel(newPeer.getLevel());
            peer.setExpToNextLevel(newPeer.getExpToNextLevel());
            peer.setStatus(newPeer.getStatus());
            peer.setCampus(newPeer.getCampus());
        } catch (Exception e) {
            log.error("Ошибка при обновлении информации о пире: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR , "Ошибка при обновлении информации о пире: " + e.getMessage());
        }
        return true;
    }

    private Mono<List<String>> getPeersName(String campusId, Integer size, Integer page) {
        String uri = String.format("/campuses/%s/participants?limit=%d&offset=%d", campusId, size, page*size);
        log.info("Запрос к API: {}", uri);

        return webClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("Ошибка получения списка пиров: {}", body);
                                    return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка получения списка пиров: " +body));
                                })
                )
                .bodyToMono(new ParameterizedTypeReference<Map<String, List<String>>>() {})
                .map(responseMap -> {
                    List<String> participants = responseMap.getOrDefault("participants", List.of());
                    log.info("Получен список участников: {}", participants);
                    return participants;
                })
                .doOnError(e -> log.error("Ошибка при получении списка пиров: ", e));
    }


    private Mono<Peer> getPeerByNameAsync(String name) {
        return webClient.get()
                .uri("/participants/{name}", name)
                .retrieve()
                .bodyToMono(Peer.class)
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
                .doOnError(e -> log.error("Ошибка при получении информации о пире: ", e));
    }


    public List<Peer> getPeers(String campusId, Integer size, Integer page, List<Status> statuses) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Peer> peerPage = peerRepository.findByCampusIdAndStatusIn(campusId, statuses, pageable);

        return peerPage.getContent();
    }

    public long getPeersCount(String campusId, List<Status> status) {
        return peerRepository.countByCampusIdAndStatusIn(campusId, status);
    }

    public PeerPageResponse searchPeers(PeerSearchRequest request) {
        log.info("Поиск пиров по запросу: {}", request);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Создаем запрос для сущностей
        CriteriaQuery<Peer> query = cb.createQuery(Peer.class);
        Root<Peer> root = query.from(Peer.class);

        List<Predicate> predicates = new ArrayList<>();

        // campusId фильтр
        if (request.getCampusId() != null && !request.getCampusId().isEmpty()) {
            predicates.add(cb.equal(root.get("campus").get("id"), request.getCampusId()));
        }

        // statuses фильтр
        if (request.getStatuses() != null && !request.getStatuses().isEmpty()) {
            predicates.add(root.get("status").in(request.getStatuses()));
        }

        // peerName фильтр (поиск по части имени)
        if (request.getPeerName() != null && !request.getPeerName().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("login")), "%" + request.getPeerName().toLowerCase() + "%"));
        }

        // применяем условия
        query.where(cb.and(predicates.toArray(new Predicate[0])));

        // пагинация
        int page = request.getPage();
        int size = request.getSize();

        List<Peer> peers = entityManager.createQuery(query)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

        // Запрос на подсчет общего количества подходящих записей
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Peer> countRoot = countQuery.from(Peer.class);

        List<Predicate> countPredicates = new ArrayList<>();

        // Копируем условия для count-запроса
        if (request.getCampusId() != null && !request.getCampusId().isEmpty()) {
            countPredicates.add(cb.equal(countRoot.get("campus").get("id"), request.getCampusId()));
        }
        if (request.getStatuses() != null && !request.getStatuses().isEmpty()) {
            countPredicates.add(countRoot.get("status").in(request.getStatuses()));
        }
        if (request.getPeerName() != null && !request.getPeerName().isEmpty()) {
            countPredicates.add(cb.like(cb.lower(countRoot.get("login")), "%" + request.getPeerName().toLowerCase() + "%"));
        }

        countQuery.select(cb.count(countRoot)).where(cb.and(countPredicates.toArray(new Predicate[0])));
        long total = entityManager.createQuery(countQuery).getSingleResult();

        log.info("Найдено {} записей", total);

        // Возвращаем наш PeerPageResponse
        return new PeerPageResponse(total, size, peers);
    }
}
