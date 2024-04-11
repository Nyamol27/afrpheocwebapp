package net.pheocnetafr.africapheocnet.service;

import net.pheocnetafr.africapheocnet.entity.Notification;
import net.pheocnetafr.africapheocnet.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification createNotification(String firstName, String email) {
        Notification notification = new Notification();
        notification.setFirstName(firstName);
        notification.setEmail(email);
        notification.setIsEnable(true); 
        return notificationRepository.save(notification);
    }

    public Optional<Notification> getNotificationByEmail(String email) {
        return notificationRepository.findByEmail(email);
    }

    @Transactional
    public void updateNotificationStatus(String email, String value) {
        boolean enableNotifications = "Enable".equalsIgnoreCase(value);
        Optional<Notification> optionalNotification = notificationRepository.findByEmail(email);
        optionalNotification.ifPresent(notification -> {
            notification.setIsEnable(enableNotifications);
            notificationRepository.save(notification);
        });
    }

    @Transactional
    public void enableNotificationStatus(String email, String firstName) {
        // Check if the user already exists
        Optional<Notification> existingNotification = notificationRepository.findByEmail(email);

        if (existingNotification.isPresent()) {
            // User exists, update the notification status
            Notification notification = existingNotification.get();
            notification.setIsEnable(true);
            notificationRepository.save(notification);
        } else {
            // User does not exist, create a new notification record
            Notification newNotification = new Notification();
            newNotification.setFirstName(firstName);
            newNotification.setEmail(email);
            newNotification.setIsEnable(true);
            notificationRepository.save(newNotification);
        }
    }

    @Transactional
    public void disableNotificationStatus(String email, String firstName) {
        // Check if the user already exists
        Optional<Notification> existingNotification = notificationRepository.findByEmail(email);

        if (existingNotification.isPresent()) {
            // User exists, update the notification status
            Notification notification = existingNotification.get();
            notification.setIsEnable(false);
            notificationRepository.save(notification);
        } else {
            // User does not exist, create a new notification record
            Notification newNotification = new Notification();
            newNotification.setFirstName(firstName);
            newNotification.setEmail(email);
            newNotification.setIsEnable(false);
            notificationRepository.save(newNotification);
        }
    }


 



}
