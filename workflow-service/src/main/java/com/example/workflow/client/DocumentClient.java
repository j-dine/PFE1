package com.example.workflow.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "document-service",
        url = "${clients.document.url:http://document-service:8086}",
        path = "/api/documents")
public interface DocumentClient {

    @PostMapping
    Object create(@RequestBody DocumentRequest request);
}