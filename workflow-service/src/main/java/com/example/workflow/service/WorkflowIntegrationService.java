package com.example.workflow.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

import com.example.workflow.client.DossierClient;
import com.example.workflow.client.DossierDTO;
import com.example.workflow.client.DocumentClient;
import com.example.workflow.client.DocumentRequest;
import com.example.workflow.client.NotificationClient;
import com.example.workflow.client.NotificationRequest;
import com.example.workflow.client.PaiementClient;
import com.example.workflow.client.PaiementRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkflowIntegrationService {

    private static final String DEFAULT_DESTINATAIRE = "agent@localhost";

    private final DossierClient dossierClient;
    private final PaiementClient paiementClient;
    private final NotificationClient notificationClient;
    private final DocumentClient documentClient;

    public void updateDossierStatus(DelegateExecution execution, String statut) {
        Long dossierId = getDossierId(execution);
        dossierClient.updateStatut(dossierId, statut);
        execution.setVariable("statut", statut);
    }

    public void createDocumentIfPresent(DelegateExecution execution) {
        Long dossierId = getDossierId(execution);

        String nomFichier = getStringVariable(execution, "documentNomFichier");
        if (nomFichier == null || nomFichier.isBlank()) {
            return;
        }

        DocumentRequest req = new DocumentRequest();
        req.setDossierId(dossierId);
        req.setNomFichier(nomFichier);
        req.setType(getStringVariable(execution, "documentType"));
        req.setUrlStorage(getStringVariable(execution, "documentUrlStorage"));

        documentClient.create(req);
    }

    public void createPaiement(DelegateExecution execution) {
        Long dossierId = getDossierId(execution);
        Double montant = getDoubleVariable(execution, "montant", 100.0d);

        PaiementRequest request = new PaiementRequest();
        request.setDossierId(dossierId);
        request.setMontant(montant);
        request.setModePaiement("VIREMENT");
        request.setStatut("VALIDE");
        request.setReference("WF-" + dossierId + "-" + UUID.randomUUID().toString().substring(0, 8));
        request.setDatePaiement(LocalDateTime.now());

        paiementClient.createPaiement(request);
    }

    public void notifyResult(DelegateExecution execution, boolean validated) {
        Long dossierId = getDossierId(execution);
        String destinataire = resolveDestinataire(execution, dossierId);
        String sujet = validated
                ? "Dossier valide et archive"
                : "Dossier rejete";
        String message = validated
                ? "Le dossier " + dossierId + " a termine le workflow (valide, paye, archive)."
                : "Le dossier " + dossierId + " a ete rejete pendant la validation.";

        NotificationRequest request = new NotificationRequest();
        request.setDestinataire(destinataire);
        request.setSujet(sujet);
        request.setMessage(message);
        request.setType("IN_APP");
        request.setDossierId(dossierId);

        notificationClient.create(request);
    }

    private Long getDossierId(DelegateExecution execution) {
        Object raw = execution.getVariable("dossierId");
        if (raw instanceof Number number) {
            return number.longValue();
        }
        throw new IllegalArgumentException("Variable dossierId absente ou invalide dans le workflow");
    }

    private Double getDoubleVariable(DelegateExecution execution, String key, Double defaultValue) {
        Object raw = execution.getVariable(key);
        if (raw instanceof Number number) {
            return number.doubleValue();
        }
        return defaultValue;
    }

    private String getStringVariable(DelegateExecution execution, String key) {
        Object raw = execution.getVariable(key);
        if (raw instanceof String value && !value.isBlank()) {
            return value;
        }
        return null;
    }

    private String resolveDestinataire(DelegateExecution execution, Long dossierId) {
        Object rawDestinataire = execution.getVariable("destinataire");
        if (rawDestinataire instanceof String value && !value.isBlank()) {
            return value;
        }

        try {
            DossierDTO dossier = dossierClient.getDossierById(dossierId);
            if (dossier != null && dossier.getUserId() != null) {
                return "user-" + dossier.getUserId() + "@localhost";
            }
        } catch (Exception ignored) {
        }

        return DEFAULT_DESTINATAIRE;
    }
}