package com.example.notification.service;

import com.example.notification.entity.Notification;
import com.example.notification.entity.StatutNotification;

import java.util.List;

public interface NotificationService {

    /** Envoie et persiste une notification */
    Notification sendNotification(String destinataire, String sujet, String message);

    /** Envoie à partir d'une entité Notification déjà construite */
    Notification send(Notification notification);

    Notification getById(Long id);

    List<Notification> getAllNotifications();

    List<Notification> getByDestinataire(String destinataire);

    List<Notification> getByDossier(Long dossierId);

    Notification markAsRead(Long id);

    void deleteNotification(Long id);
}
