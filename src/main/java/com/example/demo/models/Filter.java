package com.example.demo.models;

import lombok.Data;
import org.json.JSONObject;

@Data
public class Filter {
    private Boolean status;

    public Filter readJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            this.status = jsonObject.getBoolean("status");
        } catch (Exception e) {
            throw new RuntimeException("Error reading JSON: " + e.getMessage());
        }
        return this;
    }
}
