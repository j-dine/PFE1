package com.example.paiement.service;

import com.example.paiement.dto.PaiementDTO;
import com.example.paiement.entity.Paiement;
import com.example.paiement.entity.StatutPaiement;

import java.util.List;

public interface PaiementService {

    PaiementDTO createPaiement(Paiement paiement);

    PaiementDTO getPaiementById(Long id);

    List<PaiementDTO> getAllPaiements();

    List<PaiementDTO> getPaiementsByDossier(Long dossierId);

    PaiementDTO updateStatut(Long id, StatutPaiement statut);

    void deletePaiement(Long id);
}
