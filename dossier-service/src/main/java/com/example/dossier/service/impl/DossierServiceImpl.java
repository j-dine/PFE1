package com.example.dossier.service.impl;

import com.example.dossier.dto.DossierDTO;
import com.example.dossier.dto.HistoriqueActionDTO;
import com.example.dossier.entity.Dossier;
import com.example.dossier.entity.HistoriqueAction;
import com.example.dossier.entity.StatutDossier;
import com.example.dossier.exception.ResourceNotFoundException;
import com.example.dossier.repository.DossierRepository;
import com.example.dossier.repository.HistoriqueRepository;
import com.example.dossier.service.DossierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DossierServiceImpl implements DossierService {

    private final DossierRepository dossierRepository;
    private final HistoriqueRepository historiqueRepository;

    @Override
    @Transactional
    public DossierDTO createDossier(Dossier dossier) {
        if (dossier == null) {
            throw new IllegalArgumentException("Dossier obligatoire");
        }

        // 1) Persist dossier
        Dossier saved = dossierRepository.save(dossier);

        // 2) Add history entry
        appendHistorique(saved, "CREATION", defaultActor(saved), "Creation du dossier",
                null, saved.getStatut() != null ? saved.getStatut().name() : null, null, null);

        // 3) Persist history through cascade (or direct repo)
        saved = dossierRepository.save(saved);
        return toDTO(saved);
    }

    @Override
    public DossierDTO getDossierById(Long id) {
        Dossier dossier = dossierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier non trouve avec l'id : " + id));
        return toDTO(dossier);
    }

    @Override
    public List<DossierDTO> getAllDossiers() {
        return dossierRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DossierDTO> getDossiersByUserId(Long userId) {
        return dossierRepository.findByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DossierDTO> getDossiersByStatut(StatutDossier statut) {
        return dossierRepository.findByStatut(statut).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DossierDTO updateStatut(Long id, StatutDossier newStatut) {
        if (newStatut == null) {
            throw new IllegalArgumentException("Statut obligatoire");
        }

        Dossier dossier = dossierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier non trouve avec l'id : " + id));

        // Lock rule: after ARCHIVE/CLOTURE we block further changes
        if (Boolean.TRUE.equals(dossier.getLocked())) {
            if (!newStatut.equals(dossier.getStatut())) {
                throw new IllegalArgumentException("Dossier verrouille: statut non modifiable");
            }
            return toDTO(dossier);
        }

        StatutDossier old = dossier.getStatut();
        dossier.setStatut(newStatut);

        if (newStatut == StatutDossier.ARCHIVE || newStatut == StatutDossier.CLOTURE) {
            dossier.setLocked(true);
            if (dossier.getArchivedAt() == null) {
                dossier.setArchivedAt(LocalDateTime.now());
            }
        }

        appendHistorique(dossier, "CHANGEMENT_STATUT", defaultActor(dossier), "STATUT: " + old + " -> " + newStatut,
                old != null ? old.name() : null, newStatut.name(), null, null);

        return toDTO(dossierRepository.save(dossier));
    }

    @Override
    public List<DossierDTO> search(String q, StatutDossier statut) {
        // Simple implementation to keep compatibility; can be replaced with Specifications later.
        String needle = q == null ? "" : q.trim().toLowerCase();
        return dossierRepository.findAll().stream()
                .filter(d -> statut == null || statut.equals(d.getStatut()))
                .filter(d -> {
                    if (needle.isBlank()) return true;
                    String numero = d.getNumero() == null ? "" : d.getNumero().toLowerCase();
                    String titre = d.getTitre() == null ? "" : d.getTitre().toLowerCase();
                    String sujet = d.getSujet() == null ? "" : d.getSujet().toLowerCase();
                    String service = d.getServiceCible() == null ? "" : d.getServiceCible().toLowerCase();
                    return numero.contains(needle) || titre.contains(needle) || sujet.contains(needle) || service.contains(needle);
                })
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DossierDTO updateDossier(Long id, Dossier patch) {
        if (patch == null) throw new IllegalArgumentException("Patch obligatoire");

        Dossier dossier = dossierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier non trouve avec l'id : " + id));

        if (Boolean.TRUE.equals(dossier.getLocked())) {
            throw new IllegalArgumentException("Dossier verrouille: modification impossible");
        }

        // Partial update: only apply non-null values
        if (patch.getDateReception() != null) dossier.setDateReception(patch.getDateReception());
        if (patch.getSujet() != null) dossier.setSujet(patch.getSujet());
        if (patch.getTitre() != null) dossier.setTitre(patch.getTitre());
        if (patch.getDescription() != null) dossier.setDescription(patch.getDescription());
        if (patch.getPriorite() != null) dossier.setPriorite(patch.getPriorite());
        if (patch.getTypeDossier() != null) dossier.setTypeDossier(patch.getTypeDossier());
        if (patch.getServiceCible() != null) dossier.setServiceCible(patch.getServiceCible());
        if (patch.getDestinataireExterne() != null) dossier.setDestinataireExterne(patch.getDestinataireExterne());
        if (patch.getCanalReception() != null) dossier.setCanalReception(patch.getCanalReception());
        if (patch.getTypeCourrier() != null) dossier.setTypeCourrier(patch.getTypeCourrier());
        if (patch.getDeadlineAt() != null) dossier.setDeadlineAt(patch.getDeadlineAt());
        if (patch.getUserId() != null) dossier.setUserId(patch.getUserId());

        appendHistorique(dossier, "MISE_A_JOUR", defaultActor(dossier), "Mise a jour metadonnees", null, null, null, null);
        return toDTO(dossierRepository.save(dossier));
    }

    @Override
    @Transactional
    public DossierDTO assign(Long id, String serviceCible, Long userId) {
        Dossier dossier = dossierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier non trouve avec l'id : " + id));

        if (Boolean.TRUE.equals(dossier.getLocked())) {
            throw new IllegalArgumentException("Dossier verrouille: assignation impossible");
        }

        if (serviceCible != null) dossier.setServiceCible(serviceCible);
        if (userId != null) dossier.setUserId(userId);

        appendHistorique(dossier, "ASSIGNATION", defaultActor(dossier),
                "Assigne a " + (serviceCible != null ? serviceCible : "-") + " / userId=" + (userId != null ? userId : "-"),
                null, null, null, null);
        return toDTO(dossierRepository.save(dossier));
    }

    @Override
    @Transactional
    public DossierDTO archive(Long id, Integer retentionYears, String commentaire) {
        Dossier dossier = dossierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier non trouve avec l'id : " + id));

        if (Boolean.TRUE.equals(dossier.getLocked())) {
            return toDTO(dossier);
        }

        dossier.setRetentionYears(retentionYears != null ? retentionYears : dossier.getRetentionYears());
        dossier.setArchivedAt(LocalDateTime.now());
        dossier.setLocked(true);

        StatutDossier old = dossier.getStatut();
        dossier.setStatut(StatutDossier.ARCHIVE);

        appendHistorique(dossier, "ARCHIVAGE", defaultActor(dossier),
                (commentaire != null && !commentaire.isBlank()) ? commentaire : "Archivage du dossier",
                old != null ? old.name() : null, StatutDossier.ARCHIVE.name(), null, null);

        return toDTO(dossierRepository.save(dossier));
    }

    @Override
    @Transactional
    public DossierDTO addComment(Long id, String commentaire, String visibilite) {
        if (commentaire == null || commentaire.isBlank()) {
            throw new IllegalArgumentException("Commentaire obligatoire");
        }

        Dossier dossier = dossierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier non trouve avec l'id : " + id));

        // On trace un événement "COMMENTAIRE" sans changer le statut.
        String oldStatut = dossier.getStatut() != null ? dossier.getStatut().name() : null;
        appendHistorique(
                dossier,
                "COMMENTAIRE",
                defaultActor(dossier),
                commentaire,
                oldStatut,
                oldStatut,
                visibilite,
                null
        );

        return toDTO(dossierRepository.save(dossier));
    }

    @Override
    @Transactional
    public DossierDTO expedier(Long id, String modeExpedition, String commentaire) {
        Dossier dossier = dossierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier non trouve avec l'id : " + id));

        if (Boolean.TRUE.equals(dossier.getLocked())) {
            throw new IllegalArgumentException("Dossier verrouille: expédition impossible");
        }

        dossier.setDateExpedition(LocalDateTime.now());
        if (modeExpedition != null) dossier.setModeExpedition(modeExpedition);

        appendHistorique(dossier, "EXPEDITION", defaultActor(dossier),
                (commentaire != null && !commentaire.isBlank()) ? commentaire : ("Expedie via " + (modeExpedition != null ? modeExpedition : "-")),
                null, null, null, null);
        return toDTO(dossierRepository.save(dossier));
    }

    @Override
    public List<HistoriqueActionDTO> getHistorique(Long dossierId) {
        // validate dossier exists to return 404 instead of empty list
        if (!dossierRepository.existsById(dossierId)) {
            throw new ResourceNotFoundException("Dossier non trouve avec l'id : " + dossierId);
        }

        return historiqueRepository.findByDossierId(dossierId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteDossier(Long id) {
        if (!dossierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Dossier non trouve avec l'id : " + id);
        }
        dossierRepository.deleteById(id);
    }

    private void appendHistorique(
            Dossier dossier,
            String action,
            String actor,
            String commentaire,
            String fromStatut,
            String toStatut,
            String actorRole,
            String ip
    ) {
        HistoriqueAction h = new HistoriqueAction();
        h.setAction(action);
        h.setEffectuePar(actor);
        h.setCommentaire(commentaire);
        h.setFromStatut(fromStatut);
        h.setToStatut(toStatut);
        h.setActorRole(actorRole);
        h.setIp(ip);
        h.setDossier(dossier);

        if (dossier.getHistorique() == null) {
            dossier.setHistorique(new java.util.ArrayList<>());
        }
        dossier.getHistorique().add(h);
    }

    private String defaultActor(Dossier dossier) {
        if (dossier != null && dossier.getUserId() != null) {
            return "userId:" + dossier.getUserId();
        }
        return "SYSTEM";
    }

    // --- Mapping ---
    private DossierDTO toDTO(Dossier d) {
        DossierDTO dto = new DossierDTO();
        dto.setId(d.getId());
        dto.setNumero(d.getNumero());
        dto.setDateReception(d.getDateReception());
        dto.setSujet(d.getSujet());
        dto.setTitre(d.getTitre());
        dto.setDescription(d.getDescription());
        dto.setStatut(d.getStatut());
        dto.setPriorite(d.getPriorite());
        dto.setTypeDossier(d.getTypeDossier());
        dto.setServiceCible(d.getServiceCible());
        dto.setDestinataireExterne(d.getDestinataireExterne());
        dto.setCanalReception(d.getCanalReception());
        dto.setTypeCourrier(d.getTypeCourrier());
        dto.setDeadlineAt(d.getDeadlineAt());
        dto.setDateExpedition(d.getDateExpedition());
        dto.setModeExpedition(d.getModeExpedition());
        dto.setRetentionYears(d.getRetentionYears());
        dto.setArchivedAt(d.getArchivedAt());
        dto.setUserId(d.getUserId());
        dto.setDateCreation(d.getDateCreation());
        dto.setLocked(d.getLocked());
        return dto;
    }

    private HistoriqueActionDTO toDTO(HistoriqueAction h) {
        HistoriqueActionDTO dto = new HistoriqueActionDTO();
        dto.setId(h.getId());
        dto.setAction(h.getAction());
        dto.setEffectuePar(h.getEffectuePar());
        dto.setCommentaire(h.getCommentaire());
        dto.setFromStatut(h.getFromStatut());
        dto.setToStatut(h.getToStatut());
        dto.setActorRole(h.getActorRole());
        dto.setIp(h.getIp());
        dto.setDateAction(h.getDateAction());
        return dto;
    }
}

