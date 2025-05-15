package com.example.demo.models.http;

import com.example.demo.models.dto.Peer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class PeerPageResponse {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private long total;
    private int size;
    private List<Peer> peers;

    @Override
    public String toString() {
        try {

            return MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка сериализации PeerPageResponse в JSON", e);
        }
    }
}
