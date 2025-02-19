package com.example.demo.controller;

import com.example.demo.models.Filter;
import com.example.demo.service.KafkaProducerService;
import com.example.demo.service.PeerService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.json.JSONObject;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class PeerController {
    private final KafkaProducerService kafkaProducer;
    private final PeerService peerService;

    @KafkaListener(topics = "peers", groupId = "peer_service")
    public void consumeMessage(ConsumerRecord<String, String> message) {
        System.out.println("Received message: " + message.value());
        Filter filter = new Filter().readJson(message.value());

        JSONObject jsonObject = new JSONObject(message.value());
        Long page = jsonObject.getLong("page");
        Long size = jsonObject.getLong("size");
//        String campusId = jsonObject.getString("campusId");
//        String messageValue = peerService.getPeers(page, size,campusId, filter);
//        kafkaProducer.sendMessage("response", message.key(), messageValue);
    }
}

