package com.example.document.config;

import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Component
public class EndpointsLogger {

    private static final Logger log = LoggerFactory.getLogger(EndpointsLogger.class);

    private final RequestMappingHandlerMapping mapping;

    public EndpointsLogger(RequestMappingHandlerMapping mapping) {
        this.mapping = mapping;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        mapping.getHandlerMethods().entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().toString()))
                .map(e -> {
                    RequestMappingInfo info = e.getKey();
                    return info + " -> " + e.getValue().getMethod().getDeclaringClass().getSimpleName()
                            + "#" + e.getValue().getMethod().getName();
                })
                .filter(s -> s.contains("/api/documents"))
                .forEach(s -> log.info("Mapped: {}", s));
    }
}

