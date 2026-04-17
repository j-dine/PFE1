package com.example.workflow.service.impl;

import com.example.workflow.dto.WorkflowRequest;
import com.example.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

    private final RuntimeService runtimeService;

    private static final String DEFAULT_PROCESS_KEY = "dossier-process";

    @Override
    public String startDossierProcess(Long dossierId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("dossierId", dossierId);
        variables.put("isValidated", true);
        ProcessInstance instance = runtimeService
                .startProcessInstanceByKey(DEFAULT_PROCESS_KEY, variables);
        return instance.getId();
    }

    @Override
    public String startProcess(WorkflowRequest request) {
        String key = (request.getProcessKey() != null && !request.getProcessKey().isBlank())
                ? request.getProcessKey()
                : DEFAULT_PROCESS_KEY;

        Map<String, Object> variables = new HashMap<>();
        variables.put("dossierId", request.getDossierId());
        variables.put("isValidated", request.getIsValidated() != null ? request.getIsValidated() : true);

        if (request.getCommentaire() != null) {
            variables.put("commentaire", request.getCommentaire());
        }
        if (request.getMontant() != null) {
            variables.put("montant", request.getMontant());
        }
        if (request.getDestinataire() != null && !request.getDestinataire().isBlank()) {
            variables.put("destinataire", request.getDestinataire());
        }

        if (request.getDocumentNomFichier() != null && !request.getDocumentNomFichier().isBlank()) {
            variables.put("documentNomFichier", request.getDocumentNomFichier());
        }
        if (request.getDocumentType() != null && !request.getDocumentType().isBlank()) {
            variables.put("documentType", request.getDocumentType());
        }
        if (request.getDocumentUrlStorage() != null && !request.getDocumentUrlStorage().isBlank()) {
            variables.put("documentUrlStorage", request.getDocumentUrlStorage());
        }

        ProcessInstance instance = runtimeService.startProcessInstanceByKey(key, variables);
        return instance.getId();
    }

    @Override
    public String getProcessStatus(String processInstanceId) {
        ProcessInstance instance = runtimeService
                .createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (instance == null) {
            return "COMPLETED";
        }
        return instance.isSuspended() ? "SUSPENDED" : "ACTIVE";
    }
}