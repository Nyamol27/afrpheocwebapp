package net.pheocnetafr.africapheocnet.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import net.pheocnetafr.africapheocnet.entity.Trainer;
import net.pheocnetafr.africapheocnet.service.TrainerService;

@Component
public class TrainerStatusUpdateScheduler {

    private static final String EMAIL_TEMPLATE_PATH = "classpath:static/status_update_notification.html";

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private ResourceLoader resourceLoader;

    @Scheduled(cron = "0 0 0 1 * *") 
    public void checkAndUpdateTrainerStatus() {
       
        List<Trainer> trainers = trainerService.getAllTrainers();
        LocalDate currentDate = LocalDate.now();

        for (Trainer trainer : trainers) {
            LocalDate lastUpdateDate = LocalDate.parse(trainer.getLastUpdate());

            long daysSinceLastUpdate = ChronoUnit.DAYS.between(lastUpdateDate, currentDate);

            if (daysSinceLastUpdate > 30) {
                sendStatusUpdateNotification(trainer);
            }
        }
    }

    private String loadEmailTemplate() throws IOException {
        Resource resource = resourceLoader.getResource(EMAIL_TEMPLATE_PATH);
        try (InputStream inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        }
    }

    private void sendStatusUpdateNotification(Trainer trainer) {
        // Get trainer's email address
        String trainerEmail = trainer.getEmail();

        try {
            // Load email template content
            String emailTemplate = loadEmailTemplate();

            // Populate email content with trainer information
            String emailContent = emailTemplate.replace("[[TRAINER_NAME]]", trainer.getFirstName());

            // Create MimeMessageHelper for HTML email
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(trainerEmail);
            helper.setSubject("Status Update Reminder");
            helper.setText(emailContent, true);

            // Send the email
            javaMailSender.send(message);
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }
}
