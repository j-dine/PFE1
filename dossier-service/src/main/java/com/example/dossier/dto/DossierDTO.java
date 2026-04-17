package com.example.dossier.dto;

import com.example.dossier.entity.PrioriteDossier;
import com.example.dossier.entity.StatutDossier;
import com.example.dossier.entity.CanalReception;
import com.example.dossier.entity.TypeCourrier;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DossierDTO {

    private Long id;
    private String numero;
    private LocalDate dateReception;
    private String sujet;
    private String titre;
    private String description;
    private StatutDossier statut;
    private PrioriteDossier priorite;
    private String typeDossier;
    private String serviceCible;
    private String destinataireExterne;
    private CanalReception canalReception;
    private TypeCourrier typeCourrier;
    private LocalDateTime deadlineAt;
    private LocalDateTime dateExpedition;
    private String modeExpedition;
    private Integer retentionYears;
    private LocalDateTime archivedAt;
    private Long userId;
    private LocalDateTime dateCreation;
    private Boolean locked;
}
