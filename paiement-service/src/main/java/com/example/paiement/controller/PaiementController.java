package com.example.paiement.controller;

import com.example.paiement.dto.PaiementDTO;
import com.example.paiement.entity.Paiement;
import com.example.paiement.entity.StatutPaiement;
import com.example.paiement.service.PaiementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
public class PaiementController {

    private final PaiementService paiementService;

    @PostMapping
    public ResponseEntity<PaiementDTO> createPaiement(@RequestBody Paiement paiement) {
        return ResponseEntity.ok(paiementService.createPaiement(paiement));
    }

    @GetMapping
    public ResponseEntity<List<PaiementDTO>> getAllPaiements() {
        return ResponseEntity.ok(paiementService.getAllPaiements());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaiementDTO> getPaiementById(@PathVariable Long id) {
        return ResponseEntity.ok(paiementService.getPaiementById(id));
    }

    @GetMapping("/dossier/{dossierId}")
    public ResponseEntity<List<PaiementDTO>> getPaiementsByDossier(@PathVariable Long dossierId) {
        return ResponseEntity.ok(paiementService.getPaiementsByDossier(dossierId));
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<PaiementDTO> updateStatut(
            @PathVariable Long id,
            @RequestParam StatutPaiement statut) {
        return ResponseEntity.ok(paiementService.updateStatut(id, statut));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaiement(@PathVariable Long id) {
        paiementService.deletePaiement(id);
        return ResponseEntity.noContent().build();
    }
}
