package com.example.demo.controller;

import com.example.demo.models.dto.Status;
import com.example.demo.models.http.PeerPageResponse;
import com.example.demo.models.http.PeerSearchRequest;
import com.example.demo.service.KafkaService;
import com.example.demo.service.PeerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/peers")
@RequiredArgsConstructor
@Tag(name = "Peer", description = "Контроллер для работы с пирами")
public class PeerController {
    private final KafkaService kafkaService;
    private final PeerService peerService;

    @GetMapping
    @Operation(summary = "Получить список пиров", description = "Возвращает список пиров по фильтрам: campusId, statuses, peerName, page, size")
    public ResponseEntity<PeerPageResponse> getPeers(
            @RequestParam(defaultValue = "46e7d965-21e9-4936-bea9-f5ea0d1fddf2") String campusId,
            @RequestParam(required = false) List<Status> statuses,
            @RequestParam(required = false) String peerName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Получение списка пиров: campusId={}, statuses={}, peerName={}, page={}, size={}",
                campusId, statuses, peerName, page, size);

        PeerSearchRequest request = new PeerSearchRequest();
        request.setCampusId(campusId);
        request.setStatuses(statuses);
        request.setPeerName(peerName);
        request.setPage(page);
        request.setSize(size);

        PeerPageResponse response = peerService.searchPeers(request);
        return ResponseEntity.ok(response);
    }


    @KafkaListener(topics = "peers", groupId = "peer_service")
    public void consumeMessage(ConsumerRecord<String, String> message) {
        try {
            log.info("Received Kafka message: {}", message.value());
            PeerSearchRequest request = new PeerSearchRequest().readJson(message.value());
            PeerPageResponse response = peerService.searchPeers(request);
            log.info("Processed Kafka message: {}", response);
            kafkaService.sendMessage(message.key(), response.toString());

        } catch (Exception e) {
            log.error("Error processing Kafka message: {}", e.getMessage(), e);
            String errorJson = String.format("{\"error\": \"Error processing Kafka message\", \"details\": \"%s\"}", e.getMessage());
            kafkaService.sendMessage(message.key(), errorJson);
        }
    }
}
