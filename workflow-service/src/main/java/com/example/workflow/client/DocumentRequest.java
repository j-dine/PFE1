package com.example.workflow.client;

import lombok.Data;

@Data
public class DocumentRequest {

    private Long dossierId;
    private String nomFichier;
    private String type;
    private String urlStorage;
}