package com.example.demo.controller;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Controller;

@Controller
public class PeerController {

    @KafkaListener(topics = "peers", groupId = "peer_service")
    public void consumeMessage(String message) {
        System.err.println(message);
    }
}

