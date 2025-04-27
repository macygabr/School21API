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
    private final CampusService campusService;

    @Scheduled(fixedRate = 24*60*60*1000)
    public void runDailyTask() {
        campusService.updateCampuses();
        peerService.updatePeers();
    }
}