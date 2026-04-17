package com.example.workflow.service;

import com.example.workflow.dto.WorkflowRequest;

public interface WorkflowService {

    /**
     * Démarre le processus Camunda pour un dossier donné.
     * 
     * @return l'identifiant de l'instance de processus créée
     */
    String startDossierProcess(Long dossierId);

    /**
     * Démarre le processus à partir d'un objet WorkflowRequest complet.
     * 
     * @return l'identifiant de l'instance de processus créée
     */
    String startProcess(WorkflowRequest request);

    /**
     * Retourne l'état actuel d'une instance de processus.
     */
    String getProcessStatus(String processInstanceId);
}
