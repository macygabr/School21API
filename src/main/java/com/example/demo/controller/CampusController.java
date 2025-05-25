package com.example.demo.controller;

import com.example.demo.models.dto.Campus;
import com.example.demo.models.http.PeerPageResponse;
import com.example.demo.models.http.PeerSearchRequest;
import com.example.demo.service.CampusService;
import com.example.demo.service.KafkaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/campus")
@RequiredArgsConstructor
@Tag(name = "Campus", description = "Контроллер для работы с кампусами")
public class CampusController {
    private final CampusService campusService;
    private final KafkaService kafkaService;

    @GetMapping
    @Operation(summary = "Получить список кампусов", description = "Возвращает список всех кампусов")
    public ResponseEntity<String> getCampuses() {
        try {
            List<Campus> campuses = campusService.getCampuses();
            return ResponseEntity.ok(campuses.toString());
        } catch (Exception e) {
            log.error("Ошибка получения списка кампусов: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Ошибка получения списка кампусов\", \"details\": \"" + e.getMessage() + "\"}");
        }
    }

    @KafkaListener(topics = "campuses", groupId = "campus_service")
    public void consumeMessage(ConsumerRecord<String, String> message) {
        try {
            log.info("Received Kafka message: {}", message.value());
            List<Campus> response = campusService.getCampuses();
            log.info("Processed Kafka message: {}", response);
            kafkaService.sendMessage(message.key(), response.toString());
        } catch (Exception e) {
            log.error("Error processing Kafka message: {}", e.getMessage(), e);
            String errorJson = String.format("{\"error\": \"Error processing Kafka message\", \"details\": \"%s\"}", e.getMessage());
            kafkaService.sendMessage(message.key(), errorJson);
        }
    }
}
