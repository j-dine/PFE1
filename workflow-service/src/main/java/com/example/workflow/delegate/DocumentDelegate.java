package com.example.workflow.delegate;

import com.example.workflow.service.WorkflowIntegrationService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("documentDelegate")
@RequiredArgsConstructor
public class DocumentDelegate implements JavaDelegate {

    private final WorkflowIntegrationService integrationService;

    @Override
    public void execute(DelegateExecution execution) {
        integrationService.createDocumentIfPresent(execution);
    }
}