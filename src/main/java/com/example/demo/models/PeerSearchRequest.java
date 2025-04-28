package com.example.demo.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class PeerSearchRequest  {
    private String campusId;
    private List<Status> statuses;
    private String peerName;
    private int page;
    private int size;

    public PeerSearchRequest readJson(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            PeerSearchRequest peerSearchRequest = objectMapper.readValue(json, PeerSearchRequest.class);
            this.campusId = peerSearchRequest.getCampusId();
            this.statuses = peerSearchRequest.getStatuses();
            this.peerName = peerSearchRequest.getPeerName();
            this.page = peerSearchRequest.getPage();
            this.size = peerSearchRequest.getSize();
            return this;
        } catch (Exception e) {
            System.err.println("Ошибка при парсинге JSON: " + e.getMessage());
        }
        return null;
    }
}