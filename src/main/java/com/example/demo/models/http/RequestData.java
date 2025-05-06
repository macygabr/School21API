package com.example.demo.models.http;
import com.example.demo.models.dto.Status;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestData {
    private Integer size;
    private String campusId;
    private Integer page;
    private String userId;
    private List<Status> status;

    public RequestData readJson(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            RequestData requestData = objectMapper.readValue(json, RequestData.class);
            this.size = requestData.getSize();
            this.campusId = requestData.getCampusId();
            this.page = requestData.getPage();
            this.userId = requestData.getUserId();
            this.status = requestData.getStatus();
            return this;
        } catch (Exception e) {
            System.err.println("Ошибка при парсинге JSON: " + e.getMessage());
        }
        return null;
    }
}