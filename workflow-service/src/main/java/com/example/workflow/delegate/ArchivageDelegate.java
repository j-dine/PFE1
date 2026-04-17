package com.example.workflow.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import com.example.workflow.service.WorkflowIntegrationService;

import lombok.RequiredArgsConstructor;

@Component("archivageDelegate")
@RequiredArgsConstructor
public class ArchivageDelegate implements JavaDelegate {

    private final WorkflowIntegrationService integrationService;

    @Override
    public void execute(DelegateExecution execution) {
        execution.setVariable("isLocked", true);
        integrationService.updateDossierStatus(execution, "ARCHIVE");
        integrationService.notifyResult(execution, true);
    }
}