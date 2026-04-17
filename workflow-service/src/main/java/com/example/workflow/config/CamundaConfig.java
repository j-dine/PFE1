package com.example.workflow.config;

import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * Custom Camunda configuration.
 * Extends AbstractCamundaConfiguration to allow fine-tuned engine settings
 * without replacing the Spring Boot auto-configuration entirely.
 */
@Configuration
public class CamundaConfig extends AbstractCamundaConfiguration {

    @Override
    public void preInit(SpringProcessEngineConfiguration configuration) {
        // History level: FULL keeps all audit data; use NONE/AUTO in production as
        // needed
        configuration.setHistory(SpringProcessEngineConfiguration.HISTORY_FULL);

        // Disable telemetry if running offline or in air-gapped environments
        configuration.setTelemetryReporterActivate(false);
    }
}
