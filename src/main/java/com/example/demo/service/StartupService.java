package com.example.demo.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StartupService {


    @PostConstruct
    public void init() {
        String username = "yourUsername";
        String password = "yourPassword";
    }
}