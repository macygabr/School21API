package com.example.demo.service;

import com.example.demo.models.Campus;
import com.example.demo.models.Peer;
import com.example.demo.models.exception.BadRequestException;
import com.example.demo.repository.CampuseRepository;
import com.example.demo.repository.PeerRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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


@Service
@RequiredArgsConstructor
public class PeerService {
    private final PeerRepository peerRepository;
    private final CampuseRepository campuseRepository;
    private final Logger logger = LoggerFactory.getLogger(PeerService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${school.token.secret}")
    private String token;

    public void updatePeers() {
        List<Campus> campuses = campuseRepository.findAll();
        for (Campus campus : campuses) {
            int i =0;
            while(i<100) {
                List<String> peersName = getPeersName(campus.getId(), 1000, 1000*i);
                if(peersName.isEmpty()) break;
                for (String peerName : peersName) {
                    Peer peer;
                    if(peerRepository.existsByLogin(peerName)) {
                        peer = peerRepository.findByLogin(peerName);
                    } else {
                        peer = new Peer();
                        peer.setLogin(peerName);
                    }
                    if(updateInfo(peer)){
                        logger.info("Updated peer: " + peer);
                        peerRepository.save(peer);
                    }
                }
                i++;
            }
        }
    }

    private Boolean updateInfo(Peer peer) {
            try {
                String url = "https://edu-api.21-school.ru/services/21-school/api/v1/participants/"+ peer.getLogin();
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(url))
                        .header("Authorization", "Bearer "+ token)
                        .GET()
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                Peer newPeer = objectMapper.readValue(response.body() , Peer.class);
                if(newPeer.equals(peer)) return true;
                peer.setParallelName(newPeer.getParallelName());
                peer.setClassName(newPeer.getClassName());
                peer.setExpValue(newPeer.getExpValue());
                peer.setLevel(newPeer.getLevel());
                peer.setExpToNextLevel(newPeer.getExpToNextLevel());
                peer.setStatus(newPeer.getStatus());
                peer.setCampus(newPeer.getCampus());

            } catch (Exception e) {
                logger.error(e.getMessage());
                throw new BadRequestException(e.getMessage());
            }
            return false;
    }

    private List<String> getPeersName(String campusId, Integer size, Integer page) {
        Map<String, List<String>> responseMap;
        HttpResponse<String> response;
        try {
            String url = "https://edu-api.21-school.ru/services/21-school/api/v1/campuses/"+ campusId+"/participants?limit="+size+"&offset="+page*size;
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Authorization", "Bearer "+ token)
                .GET()
                .build();

            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            responseMap = objectMapper.readValue(response.body(), new TypeReference<>() {});
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new BadRequestException(e.getMessage());
        }
        return responseMap.get("participants");
    }

    @Transactional
    public String getPeers(String campusId, Integer size, Integer page) {
        String peersJson;
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Peer> peersPage = peerRepository.findByCampusId(campusId, pageable);
            List<Peer> peers = peersPage.getContent();

            peersJson = new ObjectMapper().writeValueAsString(peers);
        } catch (Exception e) {
            logger.error(e.getMessage());
            peersJson = e.getMessage();
        }
        return peersJson;
    }
}
