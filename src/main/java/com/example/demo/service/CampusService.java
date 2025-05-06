package com.example.demo.service;

import com.example.demo.models.dto.Campus;
import com.example.demo.repository.CampusRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampusService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CampusRepository campusRepository;
    private final AuthService authService;

    public List<Campus> getCampuses() {
        return campusRepository.findAll();
    }

    public void updateCampuses() {
        try {
            String responseBody = requestSchoolApi();

            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode campusesNode = rootNode.get("campuses");
            log.info("Start init campuses: {}", campusesNode);
            if (campusesNode != null && campusesNode.isArray()) {
                for (JsonNode campusNode : campusesNode) {
                    if (!campusRepository.existsById(campusNode.get("id").asText())) {
                        Campus campus = new Campus();
                        campus.setId(campusNode.get("id").asText());
                        campus.setShortName(campusNode.get("shortName").asText());
                        campus.setFullName(campusNode.get("fullName").asText());
                        campusRepository.save(campus);
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private String requestSchoolApi() throws URISyntaxException, IOException, InterruptedException {
        String url = "https://edu-api.21-school.ru/services/21-school/api/v1/campuses";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Authorization", "Bearer "+ authService.getAccessToken())
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
