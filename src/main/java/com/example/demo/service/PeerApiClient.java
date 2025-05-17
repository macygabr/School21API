package com.example.demo.service;

import com.example.demo.models.dto.Peer;
import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PeerApiClient {

    private final AuthService authService;

    private WebClient buildClient() {
        return WebClient.builder()
                .baseUrl("https://edu-api.21-school.ru/services/21-school/api/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + authService.getValidAccessToken())
                .clientConnector(
                        new ReactorClientHttpConnector(
                                HttpClient.create()
                                        .responseTimeout(Duration.ofSeconds(10))
                                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                        )
                )                .build();
    }

    public Mono<List<String>> fetchPeerNames(String campusId, int limit, int offset) {
        String uri = String.format("/campuses/%s/participants?limit=%d&offset=%d", campusId, limit, offset);

        return buildClient()
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, List<String>>>() {})
                .retryWhen(Retry.backoff(3, Duration.ofMillis(1000)))
                .map(map -> map.getOrDefault("participants", List.of()));
    }

    public Mono<Peer> fetchPeerByLogin(String login) {
        return buildClient()
                .get()
                .uri("/participants/{login}", login)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    return Mono.error(new RuntimeException("Ошибка от API: " + response.statusCode() + " - " + errorBody));
                                })
                )
                .bodyToMono(Peer.class)
                .retryWhen(Retry.backoff(5, Duration.ofMillis(1000)))
                .doOnError(error -> log.warn("Ошибка при получении пира {}: {}", login, error.toString()));
    }


}
