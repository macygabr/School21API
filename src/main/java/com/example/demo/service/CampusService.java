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
    private String token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ5V29landCTmxROWtQVEpFZnFpVzRrc181Mk1KTWkwUHl2RHNKNlgzdlFZIn0.eyJleHAiOjE3NDU3Njc3OTUsImlhdCI6MTc0NTczMTc5NSwiYXV0aF90aW1lIjoxNzQ1NzMxNzk1LCJqdGkiOiJlOWM4NTA0My0wMWI3LTRlNDItOTFjMy02NmI5NTk3NGMzNTgiLCJpc3MiOiJodHRwczovL2F1dGguc2JlcmNsYXNzLnJ1L2F1dGgvcmVhbG1zL0VkdVBvd2VyS2V5Y2xvYWsiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiNTJkYzRmYTQtOTYyYy00OGI4LWJiYmMtOGUzOWRlODY5MTY3IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoic2Nob29sMjEiLCJub25jZSI6IjQ1YWVmNDVmLTUzODctNDQxMy04ZWMwLTMzZTI0ZTQ5Nzk3NCIsInNlc3Npb25fc3RhdGUiOiJkZmJlNzVlYi1lYzNhLTQ0ODYtYTg3Zi1lZTQ2MTAxOWYwNTgiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHBzOi8vZWR1LjIxLXNjaG9vbC5ydSIsImh0dHBzOi8vZWR1LWFkbWluLjIxLXNjaG9vbC5ydSJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiZGVmYXVsdC1yb2xlcy1lZHVwb3dlcmtleWNsb2FrIiwib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoib3BlbmlkIHByb2ZpbGUgZW1haWwiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwidXNlcl9pZCI6IjlmNDI5NjVlLTMxZTQtNGE3MS1iMzZiLTVjNWJjMDYxYjU5ZSIsIm5hbWUiOiJNYWN5IEdhYnJpZWxsZSIsImF1dGhfdHlwZV9jb2RlIjoiZGVmYXVsdCIsInByZWZlcnJlZF91c2VybmFtZSI6Im1hY3lnYWJyIiwiZ2l2ZW5fbmFtZSI6Ik1hY3kiLCJmYW1pbHlfbmFtZSI6IkdhYnJpZWxsZSIsImVtYWlsIjoibWFjeWdhYnJAc3R1ZGVudC4yMS1zY2hvb2wucnUifQ.sS7GMOGSZaVYTp9PPhXzjQri9U2DfvbfjC6xRVOvI5yq28OBm1_VDte7frQ2k4sl1rgEibfEEcYg5LjWZJnG_64r4WSEDbt_9yxBY-t2FgltNx36n35Ny-B1NT0H7Bp3OlDcYnPGenv7dXPklevFJ93nBFWdL4leNTtCs5r9jwtno3u1eLDA2mMft8sPprlWmtSboXJ_gdFDdMxq1sSvgkXbnL483RvAjhJkD5jnNhOhHz_Cz-sjFDQfe5zYbM4Y7chtKBBlXiETJSoAMGCYXK430uMFffTYadewIRj41tHP1tOUMJwGGANg5FB5HPBKkz0FkjtNTG31LesZX-8DZw";
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
