package com.example.workflow.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import com.example.workflow.service.WorkflowIntegrationService;

import lombok.RequiredArgsConstructor;

@Component("validationDelegate")
@RequiredArgsConstructor
public class ValidationDelegate implements JavaDelegate {

    private final WorkflowIntegrationService integrationService;

    @Override
    public void execute(DelegateExecution execution) {
        Object decision = execution.getVariable("isValidated");
        boolean isValidated = decision instanceof Boolean ? (Boolean) decision : true;
        execution.setVariable("isValidated", isValidated);
        integrationService.updateDossierStatus(execution, isValidated ? "VALIDE" : "REJETE");
    }
}