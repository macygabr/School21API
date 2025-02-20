package com.example.demo.controller;

import com.example.demo.models.RequestData;
import com.example.demo.service.KafkaService;
import com.example.demo.service.PeerService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class PeerController {
    private final KafkaService kafkaService;
    private final PeerService peerService;
    private final Logger logger = LoggerFactory.getLogger(PeerController.class);

    @KafkaListener(topics = "peers", groupId = "peer_service")
    public void consumeMessage(ConsumerRecord<String, String> message) {
        String peersJson = "";
        try {
            RequestData requestData = new RequestData().readJson(message.value());
            peersJson = peerService.getPeers(requestData.getCampusId(), requestData.getSize(), requestData.getPage());
        } catch (Exception e) {
            logger.error(e.getMessage());
            peersJson = e.getMessage();
        } finally {
            kafkaService.sendMessage(message.key() , peersJson);
        }
    }
}

