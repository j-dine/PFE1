package com.example.paiement.dto;

import com.example.paiement.entity.StatutPaiement;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaiementDTO {

    private Long id;
    private Long dossierId;
    private Double montant;
    private String modePaiement;
    private StatutPaiement statut;
    private String reference;
    private LocalDateTime datePaiement;
}
