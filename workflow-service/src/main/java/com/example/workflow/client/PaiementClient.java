package com.example.workflow.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "paiement-service",
        url = "${clients.paiement.url:http://paiement-service:8084}",
        path = "/api/paiements")
public interface PaiementClient {

    @PostMapping
    Object createPaiement(@RequestBody PaiementRequest request);
}