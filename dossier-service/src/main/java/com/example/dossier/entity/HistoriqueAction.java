package com.example.dossier.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "historique_actions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action; // e.g. "CREATION", "VALIDATION", "REJET"
    private String effectuePar; // username or userId
    private String commentaire;

    // Champs de traçabilité supplémentaires (optionnels pour compatibilité DB)
    private String fromStatut;
    private String toStatut;
    private String actorRole;
    private String ip;

    private LocalDateTime dateAction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id")
    private Dossier dossier;

    @PrePersist
    protected void onCreate() {
        dateAction = LocalDateTime.now();
    }
}
