package com.example.workflow.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "dossier-service",
        url = "${clients.dossier.url:http://dossier-service:8082}",
        path = "/api/dossiers")
public interface DossierClient {

    @GetMapping("/{id}")
    DossierDTO getDossierById(@PathVariable("id") Long id);

    @PutMapping("/{id}/statut")
    DossierDTO updateStatut(@PathVariable("id") Long id, @RequestParam("statut") String statut);
}
