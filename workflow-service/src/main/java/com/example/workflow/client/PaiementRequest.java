package com.example.workflow.client;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PaiementRequest {

    private Long dossierId;
    private Double montant;
    private String modePaiement;
    private String statut;
    private String reference;
    private LocalDateTime datePaiement;
}