package com.example.dossier.controller;

import com.example.dossier.dto.DossierDTO;
import com.example.dossier.dto.HistoriqueActionDTO;
import com.example.dossier.entity.Dossier;
import com.example.dossier.entity.StatutDossier;
import com.example.dossier.service.DossierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dossiers")
@RequiredArgsConstructor
public class DossierController {

    private final DossierService dossierService;

    @PostMapping
    public ResponseEntity<DossierDTO> createDossier(@Valid @RequestBody Dossier dossier) {
        return ResponseEntity.ok(dossierService.createDossier(dossier));
    }

    @GetMapping
    public ResponseEntity<List<DossierDTO>> getAllDossiers(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) StatutDossier statut
    ) {
        if ((q != null && !q.isBlank()) || statut != null) {
            return ResponseEntity.ok(dossierService.search(q, statut));
        }
        return ResponseEntity.ok(dossierService.getAllDossiers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DossierDTO> getDossierById(@PathVariable Long id) {
        return ResponseEntity.ok(dossierService.getDossierById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DossierDTO>> getDossiersByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(dossierService.getDossiersByUserId(userId));
    }

    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<DossierDTO>> getDossiersByStatut(@PathVariable StatutDossier statut) {
        return ResponseEntity.ok(dossierService.getDossiersByStatut(statut));
    }

    @PutMapping("/{id}/statut")
    public ResponseEntity<DossierDTO> updateStatut(
            @PathVariable Long id,
            @RequestParam StatutDossier statut) {
        return ResponseEntity.ok(dossierService.updateStatut(id, statut));
    }

    /**
     * Mise à jour partielle (métadonnées / traitement).
     * Gardé volontairement permissif pour ne pas casser l'existant.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<DossierDTO> patchDossier(@PathVariable Long id, @RequestBody Dossier patch) {
        return ResponseEntity.ok(dossierService.updateDossier(id, patch));
    }

    /** Distribution / assignation vers service/agent */
    @PostMapping("/{id}/assign")
    public ResponseEntity<DossierDTO> assign(
            @PathVariable Long id,
            @RequestParam(required = false) String serviceCible,
            @RequestParam(required = false) Long userId
    ) {
        return ResponseEntity.ok(dossierService.assign(id, serviceCible, userId));
    }

    /** Archivage (verrouillage + conservation) */
    @PostMapping("/{id}/archive")
    public ResponseEntity<DossierDTO> archive(
            @PathVariable Long id,
            @RequestParam(required = false) Integer retentionYears,
            @RequestParam(required = false) String commentaire
    ) {
        return ResponseEntity.ok(dossierService.archive(id, retentionYears, commentaire));
    }

    /** Courrier départ: expédier */
    @PostMapping("/{id}/expedier")
    public ResponseEntity<DossierDTO> expedier(
            @PathVariable Long id,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) String commentaire
    ) {
        return ResponseEntity.ok(dossierService.expedier(id, mode, commentaire));
    }

    @GetMapping("/{id}/historique")
    public ResponseEntity<List<HistoriqueActionDTO>> getHistorique(@PathVariable Long id) {
        return ResponseEntity.ok(dossierService.getHistorique(id));
    }

    /** Ajout d'un commentaire à l'historique (sans changement de statut) */
    @PostMapping("/{id}/comments")
    public ResponseEntity<DossierDTO> addComment(
            @PathVariable Long id,
            @RequestParam String commentaire,
            @RequestParam(required = false) String visibilite
    ) {
        return ResponseEntity.ok(dossierService.addComment(id, commentaire, visibilite));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDossier(@PathVariable Long id) {
        dossierService.deleteDossier(id);
        return ResponseEntity.noContent().build();
    }
}
