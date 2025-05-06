package com.example.demo.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledTaskService {

    private final PeerService peerService;
    private final CampusService campusService;
    private final AuthService authService;

    @Scheduled(cron = "0 0 0 * * *")
    public void runDailyTask() {
        campusService.updateCampuses();
        peerService.updatePeers();
    }
}