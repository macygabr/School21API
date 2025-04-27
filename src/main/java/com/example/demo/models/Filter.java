package com.example.demo.models;

import lombok.Data;

import java.util.List;

@Data
public class Filter {
    private List<Status> status;
}