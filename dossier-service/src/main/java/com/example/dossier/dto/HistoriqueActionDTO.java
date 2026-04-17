package com.example.dossier.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HistoriqueActionDTO {

    private Long id;
    private String action;
    private String effectuePar;
    private String commentaire;
    private String fromStatut;
    private String toStatut;
    private String actorRole;
    private String ip;
    private LocalDateTime dateAction;
}
