package com.example.ollama.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String message;
    private String model;
}
