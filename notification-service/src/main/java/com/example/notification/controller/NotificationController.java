package com.example.notification.controller;

import com.example.notification.entity.Notification;
import com.example.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** Envoi rapide : destinataire + sujet + message en paramètres */
    @PostMapping("/send")
    public ResponseEntity<Notification> send(
            @RequestParam String destinataire,
            @RequestParam String sujet,
            @RequestParam String message) {
        return ResponseEntity.ok(notificationService.sendNotification(destinataire, sujet, message));
    }

    /** Envoi complet via un objet Notification (body JSON) */
    @PostMapping
    public ResponseEntity<Notification> create(@RequestBody Notification notification) {
        return ResponseEntity.ok(notificationService.send(notification));
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getAll() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getById(id));
    }

    @GetMapping("/destinataire/{destinataire}")
    public ResponseEntity<List<Notification>> getByDestinataire(@PathVariable String destinataire) {
        return ResponseEntity.ok(notificationService.getByDestinataire(destinataire));
    }

    @GetMapping("/dossier/{dossierId}")
    public ResponseEntity<List<Notification>> getByDossier(@PathVariable Long dossierId) {
        return ResponseEntity.ok(notificationService.getByDossier(dossierId));
    }

    @PatchMapping("/{id}/lu")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
