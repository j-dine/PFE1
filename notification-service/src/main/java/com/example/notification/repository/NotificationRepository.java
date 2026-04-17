package com.example.notification.repository;

import com.example.notification.entity.Notification;
import com.example.notification.entity.StatutNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByDestinataire(String destinataire);

    List<Notification> findByStatut(StatutNotification statut);

    List<Notification> findByDossierId(Long dossierId);
}
