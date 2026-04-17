package com.example.workflow.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class TaskDTO {
    private String id;
    private String name;
    private String taskDefinitionKey;
    private String assignee;
    private String processInstanceId;
    private Long dossierId;
    private Instant created;
}

