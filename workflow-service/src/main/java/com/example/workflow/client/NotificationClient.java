package com.example.workflow.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "notification-service",
        url = "${clients.notification.url:http://notification-service:8085}",
        path = "/api/notifications")
public interface NotificationClient {

    @PostMapping
    Object create(@RequestBody NotificationRequest request);
}