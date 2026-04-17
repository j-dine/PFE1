package com.example.paiement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "paiements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long dossierId;

    @Column(nullable = false)
    private Double montant;

    private String modePaiement; // ex: VIREMENT, CHEQUE, ESPECES

    @Enumerated(EnumType.STRING)
    private StatutPaiement statut;

    private String reference; // numéro de reçu ou référence externe

    private LocalDateTime datePaiement;

    @PrePersist
    protected void onCreate() {
        datePaiement = LocalDateTime.now();
        if (statut == null) {
            statut = StatutPaiement.EN_ATTENTE;
        }
    }
}
