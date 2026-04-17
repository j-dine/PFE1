package com.example.paiement.service.impl;

import com.example.paiement.dto.PaiementDTO;
import com.example.paiement.entity.Paiement;
import com.example.paiement.entity.StatutPaiement;
import com.example.paiement.exception.ResourceNotFoundException;
import com.example.paiement.repository.PaiementRepository;
import com.example.paiement.service.PaiementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaiementServiceImpl implements PaiementService {

    private final PaiementRepository paiementRepository;

    @Override
    public PaiementDTO createPaiement(Paiement paiement) {
        return toDTO(paiementRepository.save(paiement));
    }

    @Override
    public PaiementDTO getPaiementById(Long id) {
        Paiement p = paiementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé avec l'id : " + id));
        return toDTO(p);
    }

    @Override
    public List<PaiementDTO> getAllPaiements() {
        return paiementRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaiementDTO> getPaiementsByDossier(Long dossierId) {
        return paiementRepository.findByDossierId(dossierId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PaiementDTO updateStatut(Long id, StatutPaiement statut) {
        Paiement p = paiementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé avec l'id : " + id));
        p.setStatut(statut);
        return toDTO(paiementRepository.save(p));
    }

    @Override
    public void deletePaiement(Long id) {
        if (!paiementRepository.existsById(id)) {
            throw new ResourceNotFoundException("Paiement non trouvé avec l'id : " + id);
        }
        paiementRepository.deleteById(id);
    }

    // ── Mapping ──────────────────────────────────────────────────────────────
    private PaiementDTO toDTO(Paiement p) {
        PaiementDTO dto = new PaiementDTO();
        dto.setId(p.getId());
        dto.setDossierId(p.getDossierId());
        dto.setMontant(p.getMontant());
        dto.setModePaiement(p.getModePaiement());
        dto.setStatut(p.getStatut());
        dto.setReference(p.getReference());
        dto.setDatePaiement(p.getDatePaiement());
        return dto;
    }
}
