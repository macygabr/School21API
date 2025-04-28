package com.example.demo.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PeerDto {
    private Long id;
    private String login;
    private String status;
    private String campusId;
}
