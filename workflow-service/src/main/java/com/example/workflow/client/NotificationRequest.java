package com.example.workflow.client;

import lombok.Data;

@Data
public class NotificationRequest {

    private String destinataire;
    private String sujet;
    private String message;
    private String type;
    private String statut;
    private Long dossierId;
}