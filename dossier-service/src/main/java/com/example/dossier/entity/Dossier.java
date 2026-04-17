package com.example.dossier.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dossiers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dossier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String numero;

    private LocalDate dateReception;

    private String sujet;

    @NotBlank(message = "Le titre est obligatoire")
    @Column(nullable = false)
    private String titre;

    private String description;

    @Enumerated(EnumType.STRING)
    private StatutDossier statut;

    @Enumerated(EnumType.STRING)
    private PrioriteDossier priorite;

    private String typeDossier;

    /**
     * Service cible (destination interne). Garde le type String pour rester souple
     * et compatible avec le front actuel.
     */
    private String serviceCible;

    /**
     * Destinataire externe (courrier sortant).
     */
    private String destinataireExterne;

    /**
     * Canal de réception (papier/email/fax/portail). Optionnel pour compatibilité.
     */
    @Enumerated(EnumType.STRING)
    private CanalReception canalReception;

    /**
     * Type courrier (entrant/sortant/interne). Optionnel pour compatibilité.
     */
    @Enumerated(EnumType.STRING)
    private TypeCourrier typeCourrier;

    /**
     * Date limite (SLA) calculée ou saisie.
     */
    private LocalDateTime deadlineAt;

    /**
     * Informations d'expédition (courrier départ).
     */
    private LocalDateTime dateExpedition;
    private String modeExpedition;

    /**
     * Archivage / conservation.
     */
    private Integer retentionYears;
    private LocalDateTime archivedAt;

    private Long userId;

    private LocalDateTime dateCreation;

    @Column(nullable = false)
    private Boolean locked = false;

    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistoriqueAction> historique = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();

        if (numero == null || numero.isBlank()) {
            numero = "BO-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (dateReception == null) {
            dateReception = LocalDate.now();
        }
        if (statut == null) {
            statut = StatutDossier.OUVERT;
        }
        if (priorite == null) {
            priorite = PrioriteDossier.NORMALE;
        }
        if (locked == null) {
            locked = false;
        }
    }
}
