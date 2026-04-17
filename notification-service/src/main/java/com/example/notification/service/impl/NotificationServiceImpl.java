package com.example.notification.service.impl;

import com.example.notification.entity.Notification;
import com.example.notification.entity.StatutNotification;
import com.example.notification.entity.TypeNotification;
import com.example.notification.exception.ResourceNotFoundException;
import com.example.notification.repository.NotificationRepository;
import com.example.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public Notification sendNotification(String destinataire, String sujet, String message) {
        Notification notif = new Notification();
        notif.setDestinataire(destinataire);
        notif.setSujet(sujet);
        notif.setMessage(message);
        notif.setType(TypeNotification.IN_APP);
        return send(notif);
    }

    @Override
    public Notification send(Notification notification) {
        try {
            if (notification.getType() == null) {
                notification.setType(TypeNotification.IN_APP);
            }
            // Dispatch selon le type
            switch (notification.getType()) {
                case EMAIL -> log.info("[EMAIL]  â†’ {} | {}", notification.getDestinataire(), notification.getSujet());
                case SMS -> log.info("[SMS]    â†’ {} | {}", notification.getDestinataire(), notification.getMessage());
                case IN_APP -> log.info("[IN_APP] â†’ {} | {}", notification.getDestinataire(), notification.getSujet());
            }
            notification.setStatut(StatutNotification.ENVOYE);
        } catch (Exception e) {
            log.error("Ã‰chec de l'envoi de la notification : {}", e.getMessage());
            notification.setStatut(StatutNotification.ECHEC);
        }
        return notificationRepository.save(notification);
    }

    @Override
    public Notification getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("L'id ne peut pas Ãªtre null");
        }
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification non trouvÃ©e avec l'id : " + id));
    }

    @Override
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    @Override
    public List<Notification> getByDestinataire(String destinataire) {
        return notificationRepository.findByDestinataire(destinataire);
    }

    @Override
    public List<Notification> getByDossier(Long dossierId) {
        return notificationRepository.findByDossierId(dossierId);
    }

    @Override
    public Notification markAsRead(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("L'id ne peut pas Ãªtre null");
        }
        Notification notif = getById(id);
        notif.setStatut(StatutNotification.LU);
        return notificationRepository.save(notif);
    }

    @Override
    public void deleteNotification(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("L'id ne peut pas Ãªtre null");
        }
        if (!notificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Notification non trouvÃ©e avec l'id : " + id);
        }
        notificationRepository.deleteById(id);
    }
}
