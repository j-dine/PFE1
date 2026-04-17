package com.example.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String destinataire; // email, username ou userId

    @Column(nullable = false)
    private String sujet;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    private TypeNotification type; // EMAIL, SMS, IN_APP

    @Enumerated(EnumType.STRING)
    private StatutNotification statut;

    private Long dossierId; // référence optionnelle au dossier concerné

    private LocalDateTime dateEnvoi;

    @PrePersist
    protected void onCreate() {
        dateEnvoi = LocalDateTime.now();
        if (statut == null) {
            statut = StatutNotification.EN_ATTENTE;
        }
        if (type == null) {
            type = TypeNotification.IN_APP;
        }
    }
}
