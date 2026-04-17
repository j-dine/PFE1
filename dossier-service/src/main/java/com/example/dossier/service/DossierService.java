package com.example.dossier.service;

import com.example.dossier.dto.DossierDTO;
import com.example.dossier.dto.HistoriqueActionDTO;
import com.example.dossier.entity.Dossier;
import com.example.dossier.entity.StatutDossier;

import java.util.List;

public interface DossierService {

    DossierDTO createDossier(Dossier dossier);

    DossierDTO getDossierById(Long id);

    List<DossierDTO> getAllDossiers();

    List<DossierDTO> getDossiersByUserId(Long userId);

    List<DossierDTO> getDossiersByStatut(StatutDossier statut);

    DossierDTO updateStatut(Long id, StatutDossier newStatut);

    /**
     * Recherche (q + filtres optionnels). Pour ne pas casser l'existant,
     * l'implémentation peut être simple au début puis optimisée.
     */
    List<DossierDTO> search(String q, StatutDossier statut);

    /**
     * Mise à jour partielle (métadonnées / traitement).
     */
    DossierDTO updateDossier(Long id, Dossier patch);

    /**
     * Distribution / assignation (BO -> service/agent).
     */
    DossierDTO assign(Long id, String serviceCible, Long userId);

    /**
     * Archivage: verrouille le dossier et renseigne la conservation.
     */
    DossierDTO archive(Long id, Integer retentionYears, String commentaire);

    /**
     * Ajout d'un commentaire à l'historique (sans changement de statut).
     */
    DossierDTO addComment(Long id, String commentaire, String visibilite);

    /**
     * Courrier départ: enregistrement de l'expédition.
     */
    DossierDTO expedier(Long id, String modeExpedition, String commentaire);

    List<HistoriqueActionDTO> getHistorique(Long dossierId);

    void deleteDossier(Long id);
}
