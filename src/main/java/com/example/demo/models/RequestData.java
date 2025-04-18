package com.example.demo.models;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestData {
    private Integer size;
    private String campusId;
    private Integer page;
    private String userId;

    public RequestData readJson(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            RequestData requestData = objectMapper.readValue(json, RequestData.class);
            this.size = requestData.getSize();
            this.campusId = requestData.getCampusId();
            this.page = requestData.getPage();
            this.userId = requestData.getUserId();
            return this;
        } catch (Exception e) {
            System.err.println("Ошибка при парсинге JSON: " + e.getMessage());
        }
        return null;
    }
}