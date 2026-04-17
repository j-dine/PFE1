package com.example.workflow.client;

import lombok.Data;

/**
 * Minimal projection of dossier-service's DossierDTO,
 * used as the return type for the Feign client.
 */
@Data
public class DossierDTO {

    private Long id;
    private String titre;
    private String statut;
    private Long userId;
}
