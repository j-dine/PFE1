package com.example.workflow.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import com.example.workflow.service.WorkflowIntegrationService;

import lombok.RequiredArgsConstructor;

@Component("rejetDelegate")
@RequiredArgsConstructor
public class RejetDelegate implements JavaDelegate {

    private final WorkflowIntegrationService integrationService;

    @Override
    public void execute(DelegateExecution execution) {
        integrationService.updateDossierStatus(execution, "REJETE");
        integrationService.notifyResult(execution, false);
    }
}