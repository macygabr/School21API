package com.example.demo.service;

import com.example.demo.models.Campus;
import com.example.demo.repository.CampusRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampusService {

//    @Value("${school.token.secret}")
    private String token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ5V29landCTmxROWtQVEpFZnFpVzRrc181Mk1KTWkwUHl2RHNKNlgzdlFZIn0.eyJleHAiOjE3NDM2MjUxNzUsImlhdCI6MTc0MzU4OTE3NiwiYXV0aF90aW1lIjoxNzQzNTg5MTc1LCJqdGkiOiIyZjE3MmIwOC02YmE3LTRmYTItOTFmMS01N2JlZmYxZmMzMmYiLCJpc3MiOiJodHRwczovL2F1dGguc2JlcmNsYXNzLnJ1L2F1dGgvcmVhbG1zL0VkdVBvd2VyS2V5Y2xvYWsiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiZjc5ODliMDktMzQwMC00Njc3LWJiYzMtMTJkZGMzZTFhYjE2IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoic2Nob29sMjEiLCJub25jZSI6IjY1Y2M1OTUzLWUwNjUtNDkwNC05OTI4LWJjN2I5MDk0YTMyZiIsInNlc3Npb25fc3RhdGUiOiIyYTY1YzNmMS0xZGVlLTRjMGUtOTM0ZC02Y2I4OTNkODRlNjgiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHBzOi8vZWR1LjIxLXNjaG9vbC5ydSIsImh0dHBzOi8vZWR1LWFkbWluLjIxLXNjaG9vbC5ydSJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiZGVmYXVsdC1yb2xlcy1lZHVwb3dlcmtleWNsb2FrIiwib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoib3BlbmlkIHByb2ZpbGUgZW1haWwiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwidXNlcl9pZCI6Ijg4ODVjZDcwLTE1NzItNGM3OS05MjlhLWI5YWExOWMyNmFmZSIsIm5hbWUiOiJZZWxsb3dqYWNrZXQgR2FsYSIsImF1dGhfdHlwZV9jb2RlIjoiZGVmYXVsdCIsInByZWZlcnJlZF91c2VybmFtZSI6InllbGxvd2phIiwiZ2l2ZW5fbmFtZSI6IlllbGxvd2phY2tldCIsImZhbWlseV9uYW1lIjoiR2FsYSIsImVtYWlsIjoieWVsbG93amFAc3R1ZGVudC4yMS1zY2hvb2wucnUifQ.eGzUh0xlnhqGv4ucLoeSj_96noep_5TSF6lGMLkTjDZZZ1tWEuVjc7tb0bIZwAJnGfEMM8XVQbKyH_XfRvTZ3ynoPwJJrvCw0yp26j3lEgA0GIZrDGNSaG_2pUUn8COjOVbQB3aFnaq7qU6sTLV0DIofj1XHxAG4y6E9fjicxiGWf5up1MAZlrzHEkmQJ4JBzcSAG_wEzsrA0tEZhNocARlbVHxVtLiHwzTXaDqgFHmJJOwtRde7Vetu72AYA9R8Ss3bRDgaBbt8BjqBFP7wy_TW8jw9qh-vrkkSTvuPzmLsmnUuvnoQK1fS84a7ShWp9qyXGJuNbf8dHavssWoS-w";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CampusRepository campusRepository;

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
                .header("Authorization", "Bearer "+ token)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
