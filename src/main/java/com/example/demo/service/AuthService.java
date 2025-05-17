package com.example.demo.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Instant;

@Slf4j
@Service
public class AuthService {
    private final WebClient webClient;
    private final String clientId;
    private String refreshToken;
    private String accessToken;
    private Instant expiresAt;

    public AuthService(
            @Value("${auth.token.url}") String tokenUrl,
            @Value("${auth.client.id}") String clientId,
            @Value("${auth.token.refresh}") String refreshToken
    ) {
        this.clientId = clientId;
        this.refreshToken = refreshToken;
        this.webClient = WebClient.builder().baseUrl(tokenUrl).build();
    }

    @PostConstruct
    public void init() {
        refreshAccessToken();
    }

    public synchronized String getValidAccessToken() {
        if (accessToken == null || Instant.now().isAfter(expiresAt)) {
            log.info("Access token отсутствует или просрочен. Обновляем через refresh_token...");
            refreshAccessToken();
        }
        return accessToken;
    }

    private void refreshAccessToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", refreshToken);

        try {
            TokenResponse response = webClient.post()
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .bodyValue(form)
                    .retrieve()
                    .bodyToMono(TokenResponse.class)
                    .block();

            if (response != null) {
                this.accessToken = response.getAccessToken();
                this.refreshToken = response.getRefreshToken();
                this.expiresAt = Instant.now().plusSeconds(response.getExpiresIn() - 30);
                log.info("Обновлён access_token. Истекает в {}", expiresAt);
            } else {
                log.error("Пустой ответ при обновлении токена.");
            }

        } catch (Exception e) {
            log.error("Ошибка при обновлении токена: {}", e.getMessage());
        }
    }

    private record TokenResponse(
            String access_token,
            String refresh_token,
            long expires_in
    ) {
        public String getAccessToken() {
            return access_token;
        }

        public String getRefreshToken() {
            return refresh_token;
        }

        public long getExpiresIn() {
            return expires_in;
        }
    }
}
