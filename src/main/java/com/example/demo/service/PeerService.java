package com.example.demo.service;

import com.example.demo.models.Campus;
import com.example.demo.models.Peer;
import com.example.demo.models.exception.BadRequestException;
import com.example.demo.repository.CampusRepository;
import com.example.demo.repository.PeerRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PeerService {
    private final PeerRepository peerRepository;
    private final CampusRepository campusRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    //    @Value("${school.token.secret}")
    private String token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ5V29landCTmxROWtQVEpFZnFpVzRrc181Mk1KTWkwUHl2RHNKNlgzdlFZIn0.eyJleHAiOjE3NDM2MjUxNzUsImlhdCI6MTc0MzU4OTE3NiwiYXV0aF90aW1lIjoxNzQzNTg5MTc1LCJqdGkiOiIyZjE3MmIwOC02YmE3LTRmYTItOTFmMS01N2JlZmYxZmMzMmYiLCJpc3MiOiJodHRwczovL2F1dGguc2JlcmNsYXNzLnJ1L2F1dGgvcmVhbG1zL0VkdVBvd2VyS2V5Y2xvYWsiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiZjc5ODliMDktMzQwMC00Njc3LWJiYzMtMTJkZGMzZTFhYjE2IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoic2Nob29sMjEiLCJub25jZSI6IjY1Y2M1OTUzLWUwNjUtNDkwNC05OTI4LWJjN2I5MDk0YTMyZiIsInNlc3Npb25fc3RhdGUiOiIyYTY1YzNmMS0xZGVlLTRjMGUtOTM0ZC02Y2I4OTNkODRlNjgiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHBzOi8vZWR1LjIxLXNjaG9vbC5ydSIsImh0dHBzOi8vZWR1LWFkbWluLjIxLXNjaG9vbC5ydSJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiZGVmYXVsdC1yb2xlcy1lZHVwb3dlcmtleWNsb2FrIiwib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoib3BlbmlkIHByb2ZpbGUgZW1haWwiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwidXNlcl9pZCI6Ijg4ODVjZDcwLTE1NzItNGM3OS05MjlhLWI5YWExOWMyNmFmZSIsIm5hbWUiOiJZZWxsb3dqYWNrZXQgR2FsYSIsImF1dGhfdHlwZV9jb2RlIjoiZGVmYXVsdCIsInByZWZlcnJlZF91c2VybmFtZSI6InllbGxvd2phIiwiZ2l2ZW5fbmFtZSI6IlllbGxvd2phY2tldCIsImZhbWlseV9uYW1lIjoiR2FsYSIsImVtYWlsIjoieWVsbG93amFAc3R1ZGVudC4yMS1zY2hvb2wucnUifQ.eGzUh0xlnhqGv4ucLoeSj_96noep_5TSF6lGMLkTjDZZZ1tWEuVjc7tb0bIZwAJnGfEMM8XVQbKyH_XfRvTZ3ynoPwJJrvCw0yp26j3lEgA0GIZrDGNSaG_2pUUn8COjOVbQB3aFnaq7qU6sTLV0DIofj1XHxAG4y6E9fjicxiGWf5up1MAZlrzHEkmQJ4JBzcSAG_wEzsrA0tEZhNocARlbVHxVtLiHwzTXaDqgFHmJJOwtRde7Vetu72AYA9R8Ss3bRDgaBbt8BjqBFP7wy_TW8jw9qh-vrkkSTvuPzmLsmnUuvnoQK1fS84a7ShWp9qyXGJuNbf8dHavssWoS-w";


    public void updatePeers() {
        List<Campus> campuses = campusRepository.findAll();
        for (Campus campus : campuses) {
            int i = 0;
            while (i < 100) {
                try {
                    List<String> peersName = getPeersName(campus.getId(), 1000, i);
                    if (peersName.isEmpty()) break;

                    for (String peerName : peersName) {
                        Peer peer = peerRepository.findByLogin(peerName).orElseGet(() -> new Peer(peerName));
                        if (updateInfo(peer)) {
                            log.info("Updated peer: {}", peer);
                            peerRepository.save(peer);
                        }
                    }
                } catch (Exception e) {

                }
                i++;
            }
        }
    }

    private Boolean updateInfo(Peer peer) {
        if (peer == null) return false;
        try {
            String url = "https://edu-api.21-school.ru/services/21-school/api/v1/participants/" + peer.getLogin();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new BadRequestException("Ошибка запроса к API: " + response.body());
            }

            Peer newPeer = objectMapper.readValue(response.body(), Peer.class);

            if (peer.equals(newPeer)) return false; // Данные не изменились

            peer.setParallelName(newPeer.getParallelName());
            peer.setClassName(newPeer.getClassName());
            peer.setExpValue(newPeer.getExpValue());
            peer.setLevel(newPeer.getLevel());
            peer.setExpToNextLevel(newPeer.getExpToNextLevel());
            peer.setStatus(newPeer.getStatus());
            peer.setCampus(newPeer.getCampus());

            return true;
        } catch (Exception e) {
            log.error("Ошибка при обновлении информации о пире: ", e);
            throw new BadRequestException("Ошибка при обновлении информации: " + e.getMessage());
        }
    }

    private List<String> getPeersName(String campusId, Integer size, Integer page) {
        try {
            String url = "https://edu-api.21-school.ru/services/21-school/api/v1/campuses/" + campusId
                    + "/participants?limit=" + size + "&offset=" + (page * size);
            log.info("Запрос к API: {}", url);  // Логирование запроса

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new BadRequestException("Ошибка получения списка пиров: " + response.body());
            }

            String responseBody = response.body();
            if (responseBody == null || responseBody.isEmpty()) {
                throw new BadRequestException("Пустой ответ от API");
            }

            log.info("Получен ответ от API: {}", responseBody);

            Map<String, List<String>> responseMap = objectMapper.readValue(responseBody, new TypeReference<>() {});
            return responseMap.getOrDefault("participants", List.of());
        } catch (Exception e) {
            log.error("Ошибка при получении списка пиров: ", e);
            throw new BadRequestException("Ошибка при получении списка пиров: " + e.getMessage());
        }
    }


    @Transactional
    public String getPeers(String campusId, Integer size, Integer page) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Peer> peersPage = peerRepository.findByCampusId(campusId, pageable);
            List<Peer> peers = peersPage.getContent();
            return objectMapper.writeValueAsString(peers);
        } catch (Exception e) {
            log.error("Ошибка при получении списка пиров: ", e);
            throw new BadRequestException("Ошибка при получении списка пиров: " + e.getMessage());
        }
    }
}
