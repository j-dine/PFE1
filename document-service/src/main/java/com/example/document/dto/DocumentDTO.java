package com.example.document.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentDTO {

    private Long id;
    private String nomFichier;
    private String type;
    private String urlStorage;
    private LocalDateTime dateUpload;
    private Long dossierId;
}