package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduledTaskService {

    private final PeerService peerService;
    private final CampuseService campuseService;
    private final Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);

    @Scheduled(fixedRate = 60000)
    public void runDailyTask() {
        campuseService.updateCampuses();
        peerService.updatePeers();
    }
}