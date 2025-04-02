package com.example.demo.controller;

import com.example.demo.models.RequestData;
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

@Slf4j
@RestController
@RequestMapping("/api/peers")
@RequiredArgsConstructor
@Tag(name = "Peer", description = "Контроллер для работы с пирами")
public class PeerController {
    private final KafkaService kafkaService;
    private final PeerService peerService;

    @GetMapping
    @Operation(summary = "Получить список пиров", description = "Возвращает список пиров по campusId, size и page")
    public ResponseEntity<String> getPeers(@RequestParam String campusId,
                                           @RequestParam int size,
                                           @RequestParam int page) {
        try {
            String peers = peerService.getPeers(campusId, size, page);
            return ResponseEntity.ok(peers);
        } catch (Exception e) {
            log.error("Ошибка получения списка пиров: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("{\"error\": \"Ошибка получения списка пиров\", \"details\": \"" + e.getMessage() + "\"}");
        }
    }

    @KafkaListener(topics = "peers", groupId = "peer_service")
    public void consumeMessage(ConsumerRecord<String, String> message) {
        String peersJson;
        try {
            log.info("Received Kafka message: {}", message.value());
            RequestData requestData = new RequestData().readJson(message.value());
            peersJson = peerService.getPeers(requestData.getCampusId(), requestData.getSize(), requestData.getPage());
        } catch (Exception e) {
            log.error("Ошибка обработки Kafka-сообщения: {}", e.getMessage(), e);
            peersJson = String.format("{\"error\": \"Ошибка обработки сообщения\", \"details\": \"%s\"}", e.getMessage());
        }

        kafkaService.sendMessage(message.key(), peersJson);
    }
}
