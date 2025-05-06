package com.example.demo.models.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Tribe {
    private String name;
    private String url;
}
