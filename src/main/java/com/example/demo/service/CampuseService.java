package com.example.demo.service;

import com.example.demo.models.Campus;
import com.example.demo.repository.CampuseRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@RequiredArgsConstructor
public class CampuseService {

    @Value("${school.token.secret}")
    private String token;

    private final Logger logger = LoggerFactory.getLogger(CampuseService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final CampuseRepository campuseRepository;

    public void updateCampuses() {
        try {
            String responseBody = requestSchoolApi();

            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode campusesNode = rootNode.get("campuses");

            if (campusesNode != null && campusesNode.isArray()) {
                for (JsonNode campusNode : campusesNode) {
                    if (!campuseRepository.existsById(campusNode.get("id").asText())) {
                        Campus campus = new Campus();
                        campus.setId(campusNode.get("id").asText());
                        campus.setShortName(campusNode.get("shortName").asText());
                        campus.setFullName(campusNode.get("fullName").asText());
                        campuseRepository.save(campus);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private String requestSchoolApi() throws URISyntaxException, IOException, InterruptedException {
        String url = "https://edu-api.21-school.ru/services/21-school/api/v1/campuses";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Authorization", "Bearer "+ token)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
