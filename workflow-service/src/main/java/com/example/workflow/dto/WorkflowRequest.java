package com.example.workflow.dto;

import lombok.Data;

@Data
public class WorkflowRequest {

    private Long dossierId;
    private String processKey;
    private String commentaire;

    // true => branche validation/paiement, false => branche rejet
    private Boolean isValidated;

    // champs optionnels utilises par le workflow
    private Double montant;
    private String destinataire;

    // champs optionnels: creation d'un document (metadata) dans document-service
    private String documentNomFichier;
    private String documentType;
    private String documentUrlStorage;
}