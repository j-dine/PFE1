package com.example.workflow.dto;

import lombok.Data;

import java.util.Map;

@Data
public class CompleteTaskRequest {
    /**
     * Variables Camunda (key -> value).
     */
    private Map<String, Object> variables;
}

