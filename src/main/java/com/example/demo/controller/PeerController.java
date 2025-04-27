package com.example.demo.controller;

import com.example.demo.models.Peer;
import com.example.demo.models.RequestData;
import com.example.demo.models.Status;
import com.example.demo.service.KafkaService;
import com.example.demo.service.PeerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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
    @Operation(summary = "Получить список пиров", description = "Возвращает список пиров по campusId, size и page")
    public ResponseEntity<List<Peer>> getPeers(@RequestParam(defaultValue = "46e7d965-21e9-4936-bea9-f5ea0d1fddf2") String campusId,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(name = "status", required = false) List<Status> statuses) {
        return ResponseEntity.ok(peerService.getPeers(campusId, size, page, statuses));
    }

    @KafkaListener(topics = "peers", groupId = "peer_service")
    public void consumeMessage(ConsumerRecord<String, String> message) {
        String peersJson;
        try {
            log.info("Received Kafka message: {}", message.value());
            RequestData requestData = new RequestData().readJson(message.value());
            List<Peer> peers = peerService.getPeers(requestData.getCampusId(), requestData.getSize(), requestData.getPage(), requestData.getStatus());
            ObjectMapper objectMapper = new ObjectMapper();
            peersJson = objectMapper.writeValueAsString(peers);
        } catch (Exception e) {
            log.error("Error processing Kafka messages: {}", e.getMessage(), e);
            peersJson = String.format("{\"error\": \"Error processing Kafka messages\", \"details\": \"%s\"}", e.getMessage());
        }

        kafkaService.sendMessage(message.key(), peersJson);
    }
}
