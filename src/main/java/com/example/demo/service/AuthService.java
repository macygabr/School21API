package com.example.demo.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AuthService {
    @Getter
    @Value("${school.token.secret}")
    private String accessToken;

//    @Value("${refreshToken}")
    private String refreshToken;
//    @Value("${tokenUrl}")
    private final String tokenUrl = "https://auth.sberclass.ru/auth/realms/EduPowerKeycloak/protocol/openid-connect";

//    @Value("${expiresInTime}")
    private long accessTokenExpiryTime = 36000L;

    private final WebClient webClient = WebClient.builder()
            .baseUrl(tokenUrl)
            .build();

    public void updateTokens(String accessToken, String refreshToken, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiryTime = System.currentTimeMillis() + (expiresIn * 1000L);
    }
//
//    public Mono<String> refreshAccessToken() {
//        return webClient.post()
//                .uri("/token")
//                .header("Content-Type", "application/x-www-form-urlencoded")
//                .bodyValue("grant_type=refresh_token&refresh_token=" + refreshToken)
//                .retrieve()
//                .bodyToMono(TokenResponse.class)
//                .map(response -> {
//                    updateTokens(response.getAccessToken(), response.getRefreshToken(), response.getExpiresIn());
//                    return response.getAccessToken();
//                });
//    }
}
