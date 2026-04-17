package com.example.workflow.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import com.example.workflow.service.WorkflowIntegrationService;

import lombok.RequiredArgsConstructor;

@Component("receptionDelegate")
@RequiredArgsConstructor
public class ReceptionDelegate implements JavaDelegate {

    private final WorkflowIntegrationService integrationService;

    @Override
    public void execute(DelegateExecution execution) {
        integrationService.updateDossierStatus(execution, "RECU");
    }
}