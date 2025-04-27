package com.example.demo.service;

import com.example.demo.models.Peer;
import com.example.demo.models.Status;
import com.example.demo.models.exception.BadRequestException;
import com.example.demo.repository.CampusRepository;
import com.example.demo.repository.PeerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PeerService {
    private final PeerRepository peerRepository;
    private final CampusRepository campusRepository;

    //    @Value("${school.token.secret}")
    private String token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ5V29landCTmxROWtQVEpFZnFpVzRrc181Mk1KTWkwUHl2RHNKNlgzdlFZIn0.eyJleHAiOjE3NDU3Njc3OTUsImlhdCI6MTc0NTczMTc5NSwiYXV0aF90aW1lIjoxNzQ1NzMxNzk1LCJqdGkiOiJlOWM4NTA0My0wMWI3LTRlNDItOTFjMy02NmI5NTk3NGMzNTgiLCJpc3MiOiJodHRwczovL2F1dGguc2JlcmNsYXNzLnJ1L2F1dGgvcmVhbG1zL0VkdVBvd2VyS2V5Y2xvYWsiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiNTJkYzRmYTQtOTYyYy00OGI4LWJiYmMtOGUzOWRlODY5MTY3IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoic2Nob29sMjEiLCJub25jZSI6IjQ1YWVmNDVmLTUzODctNDQxMy04ZWMwLTMzZTI0ZTQ5Nzk3NCIsInNlc3Npb25fc3RhdGUiOiJkZmJlNzVlYi1lYzNhLTQ0ODYtYTg3Zi1lZTQ2MTAxOWYwNTgiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHBzOi8vZWR1LjIxLXNjaG9vbC5ydSIsImh0dHBzOi8vZWR1LWFkbWluLjIxLXNjaG9vbC5ydSJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiZGVmYXVsdC1yb2xlcy1lZHVwb3dlcmtleWNsb2FrIiwib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoib3BlbmlkIHByb2ZpbGUgZW1haWwiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwidXNlcl9pZCI6IjlmNDI5NjVlLTMxZTQtNGE3MS1iMzZiLTVjNWJjMDYxYjU5ZSIsIm5hbWUiOiJNYWN5IEdhYnJpZWxsZSIsImF1dGhfdHlwZV9jb2RlIjoiZGVmYXVsdCIsInByZWZlcnJlZF91c2VybmFtZSI6Im1hY3lnYWJyIiwiZ2l2ZW5fbmFtZSI6Ik1hY3kiLCJmYW1pbHlfbmFtZSI6IkdhYnJpZWxsZSIsImVtYWlsIjoibWFjeWdhYnJAc3R1ZGVudC4yMS1zY2hvb2wucnUifQ.sS7GMOGSZaVYTp9PPhXzjQri9U2DfvbfjC6xRVOvI5yq28OBm1_VDte7frQ2k4sl1rgEibfEEcYg5LjWZJnG_64r4WSEDbt_9yxBY-t2FgltNx36n35Ny-B1NT0H7Bp3OlDcYnPGenv7dXPklevFJ93nBFWdL4leNTtCs5r9jwtno3u1eLDA2mMft8sPprlWmtSboXJ_gdFDdMxq1sSvgkXbnL483RvAjhJkD5jnNhOhHz_Cz-sjFDQfe5zYbM4Y7chtKBBlXiETJSoAMGCYXK430uMFffTYadewIRj41tHP1tOUMJwGGANg5FB5HPBKkz0FkjtNTG31LesZX-8DZw";

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://edu-api.21-school.ru/services/21-school/api/v1")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .build();

    public void updatePeers() {
        campusRepository.findAll().forEach(campus -> {
            for (int page = 0; page < 1000; page++) {
                try {
                    List<String> peerNames = getPeersName(campus.getId(), 100, page).block();
                    if (peerNames.isEmpty()) {
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
            throw new BadRequestException("Ошибка при обновлении информации: " + e.getMessage());
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
                                    return Mono.error(new BadRequestException("Ошибка получения списка пиров: " + body));
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
}
