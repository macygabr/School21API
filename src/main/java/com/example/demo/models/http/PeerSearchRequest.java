package com.example.demo.models.http;

import com.example.demo.models.dto.Status;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.util.List;

@Data
public class PeerSearchRequest  {
    private String campusId;
    private List<Status> statuses;
    private String peerName;
    private String parallelName;
    private SortField sortField;
    private int page;
    private int size;

    public PeerSearchRequest readJson(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            PeerSearchRequest peerSearchRequest = objectMapper.readValue(json, PeerSearchRequest.class);
            this.campusId = peerSearchRequest.getCampusId();
            this.statuses = peerSearchRequest.getStatuses();
            this.peerName = peerSearchRequest.getPeerName();
            this.sortField = peerSearchRequest.getSortField();
            this.parallelName = peerSearchRequest.getParallelName();
            this.page = peerSearchRequest.getPage();
            this.size = peerSearchRequest.getSize();
            return this;
        } catch (Exception e) {
            System.err.println("Ошибка при парсинге JSON: " + e.getMessage());
        }
        return null;
    }
}


