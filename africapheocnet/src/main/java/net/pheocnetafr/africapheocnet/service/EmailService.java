package net.pheocnetafr.africapheocnet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import net.pheocnetafr.africapheocnet.entity.Announcement;
import net.pheocnetafr.africapheocnet.entity.ContactForm;
import net.pheocnetafr.africapheocnet.entity.Notification;
import net.pheocnetafr.africapheocnet.entity.Project;
import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.repository.NotificationRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private TemplateEngine templateEngine; 
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Async
    public void sendPasswordResetEmail(String firstName, String userEmail, String resetToken) {
        try {
            // Create a MimeMessage
            jakarta.mail.internet.MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            // Create a MimeMessageHelper to work with the MimeMessage
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            // Set the recipient's email address
            helper.setTo(userEmail);

            // Set the email subject
            helper.setSubject(" Africa PHEOC-Net : Password Reset");

            // Create a Thymeleaf context to render the email template
            Context context = new Context();
            context.setVariable("resetToken", resetToken); 
            context.setVariable("firstName", firstName); 

            // Get the email content from the Thymeleaf template
            String emailBody = templateEngine.process("password-reset-email", context);

            // Set the email content (HTML)
            helper.setText(emailBody, true);

            // Send the email
            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            // Handle exceptions
            e.printStackTrace();
        }
    }
    
    @Async
    public void sendPasswordResetConfirmationEmail(String userEmail) {
        try {
            // Create a MimeMessage
            jakarta.mail.internet.MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            // Create a MimeMessageHelper to work with the MimeMessage
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            // Set the recipient's email address
            helper.setTo(userEmail);
            
            Context context = new Context();
            context.setVariable("userEmail", userEmail);

            // Set the email subject for password reset confirmation
            helper.setSubject("Africa PHEOC-Net : Password Reset Confirmation");
            String emailBody = templateEngine.process("password-reset-confirmation-email", context);

             helper.setText(emailBody, true);

            // Send the email
            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            // Handle exceptions
            e.printStackTrace();
        }
    }
    
    
    @Async
    public void sendAccountDeletedEmail(String userEmail) {
        try {
            // Create a MimeMessage
            jakarta.mail.internet.MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            // Create a MimeMessageHelper to work with the MimeMessage
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            // Set the recipient's email address
            helper.setTo(userEmail);

            // Set the email subject
            helper.setSubject("Africa PHEOC-Net : Account Deleted");

            // Prepare the email content using a Thymeleaf template
            Context context = new Context();
            context.setVariable("userEmail", userEmail);

            String emailContent = templateEngine.process("account-deleted-email.html", context);

            // Set the email content (HTML)
            helper.setText(emailContent, true);

            // Send the email
            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            // Handle exceptions
            e.printStackTrace();
        }
    }
    
    
    @Async
    public void sendApplicationSubmittedEmail(String userEmail, String firstName) {
        try {
            // Create a MimeMessage
            jakarta.mail.internet.MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            // Create a MimeMessageHelper to work with the MimeMessage
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            // Set the recipient's email address
            helper.setTo(userEmail);

            // Set the email subject
            helper.setSubject("Africa PHEOC-Net :  Your Application has been Received");

            // Prepare the email content using a Thymeleaf template
            Context context = new Context();
            context.setVariable("userEmail", userEmail);
            context.setVariable("firstName", firstName);

            String emailContent = templateEngine.process("application-submitted-email.html", context);

            // Set the email content (HTML)
            helper.setText(emailContent, true);

            // Send the email
            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            // Handle exceptions
            e.printStackTrace();
        }
    }
    
    @Async
    public void sendApplicationApprovedEmail(String userEmail, String firstName, String token) {
        try {
            // Create a MimeMessage
            jakarta.mail.internet.MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            // Create a MimeMessageHelper to work with the MimeMessage
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            // Set the recipient's email address
            helper.setTo(userEmail);

            // Set the email subject
            helper.setSubject("Africa PHEOC-Net : Application Approved");

            // Prepare the email content using a Thymeleaf template
            Context context = new Context();
            context.setVariable("userEmail", userEmail);
            context.setVariable("firstName", firstName);
            context.setVariable("token", token);

            String emailContent = templateEngine.process("application-approved-email.html", context);

            // Set the email content (HTML)
            helper.setText(emailContent, true);

            // Send the email
            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            // Handle exceptions
            e.printStackTrace();
        }
    }
    
    @Async
    public void sendApplicationRejectedEmail(String userEmail, String firstName) {
        try {
            // Create a MimeMessage
            jakarta.mail.internet.MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            // Create a MimeMessageHelper to work with the MimeMessage
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            // Set the recipient's email address
            helper.setTo(userEmail);

            // Set the email subject
            helper.setSubject("Africa PHEOC-Net : Application Rejected");

            // Prepare the email content using a Thymeleaf template
            Context context = new Context();
            context.setVariable("userEmail", userEmail);
            context.setVariable("firstName", firstName);

            String emailContent = templateEngine.process("application-rejected-email.html", context);

            // Set the email content (HTML)
            helper.setText(emailContent, true);

            // Send the email
            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            // Handle exceptions
            e.printStackTrace();
        }
    }
    @Async
    public void sendEmail(String recipient, String templateName, Map<String, Object> model) {
        try {
            jakarta.mail.internet.MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Set recipient email address
            helper.setTo(recipient);

            // Load and process the email template
            String emailContent = templateEngine.process("contact-user-email", new Context(Locale.US, model));

            // Set email subject and content
            helper.setSubject((String) model.get("subject"));
            helper.setText(emailContent, true);

            // Send the email
            javaMailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace(); // Handle exceptions as needed
        }
    }
    @Async
    
    public void sendAnnouncementEmail(Announcement announcement) {
        try {
            List<String> toEmails = notificationRepository.findByIsEnable(true).stream()
                    .map(Notification::getEmail)
                    .collect(Collectors.toList());

            String subject = announcement.getSubject();
            String content = announcement.getContent();

            jakarta.mail.internet.MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom("afrpheocnet@gmail.com"); 
            helper.setBcc(toEmails.toArray(new String[0]));
            helper.setSubject(subject);

            // Set the email content using Thymeleaf template
            Context thymeleafContext = new Context();
            thymeleafContext.setVariable("content", content);
            String emailContent = templateEngine.process("announcement-email", thymeleafContext);

            helper.setText(emailContent, true);

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }


    // Method to send email with attachment
    @Async
   
    public void sendAnnouncementEmailWithAttachment(Announcement announcement, MultipartFile attachment) {
        try {
            List<String> toEmails = notificationRepository.findByIsEnable(true).stream()
                    .map(Notification::getEmail)
                    .collect(Collectors.toList());

            String subject = announcement.getSubject();
            String content = announcement.getContent();

            jakarta.mail.internet.MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom("afrpheocnet@gmail.com"); 
            helper.setBcc(toEmails.toArray(new String[0]));
            helper.setSubject(subject);

            // Set the email content using Thymeleaf template
            Context thymeleafContext = new Context();
            thymeleafContext.setVariable("content", content);
            String emailContent = templateEngine.process("announcement-email", thymeleafContext);

            helper.setText(emailContent, true);

            if (attachment != null && !attachment.isEmpty()) {
                // Attach the file from MultipartFile
                ByteArrayResource byteArrayResource = new ByteArrayResource(attachment.getBytes());
                helper.addAttachment(attachment.getOriginalFilename(), byteArrayResource);
            }

            javaMailSender.send(mimeMessage);
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }


    
    @Async
    public void sendMembershipRequestEmail(String toEmail, String subject, String content) {
        try {
            jakarta.mail.internet.MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true);

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    @Async
    public void sendMembershipApprovalEmail(String toEmail, String subject, String content) {
        try {
            jakarta.mail.internet.MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true);

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    @Async
    public void sendMembershipRejectionEmail(String toEmail, String subject, String content) {
        try {
            jakarta.mail.internet.MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true);

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    @Async
    public void sendInvitationEmail(String recipientEmail, String subject, String templateName, Context context) throws MessagingException {
        jakarta.mail.internet.MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(recipientEmail);
        helper.setSubject(subject);

        // Process the email template using Thymeleaf
        String emailContent = templateEngine.process(templateName, context);

        helper.setText(emailContent, true);

        javaMailSender.send(message);
    }
    @Async
    public void sendAccountCreationEmail(User user, String subject, String content) {
        try {
            // Prepare template variables
            Context context = new Context();
            context.setVariable("firstName", user.getFirstName());
            context.setVariable("lastName", user.getLastName());
            // Add more variables as needed...

            // Process the Thymeleaf template
            String processedContent = templateEngine.process("account-creation-email", context);

            // Create and send the email
            jakarta.mail.internet.MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText(processedContent, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    @Async
    public void sendProjectCreationEmail(String recipient, Project project) throws MessagingException {
    	jakarta.mail.internet.MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        try {
            helper.setTo(recipient);
            helper.setSubject("Africa PHEOC-Net : New Project Created: " + project.getProjectName());
            String content = buildEmailContent(project);
            helper.setText(content, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
           
            e.printStackTrace(); 
            throw e; 
        }
    }

    private String buildEmailContent(Project project) {
        Context context = new Context();
        context.setVariable("project", project);
        return templateEngine.process("project-creation-email-template", context);
    }
    @Async
    public void sendNewTopicEmail(List<String> toEmails, String subject, String topicTitle, String categoryName, String topicLink) {
        // Load the HTML email template
        String emailContent = loadNewTopicEmailTemplate(topicTitle, categoryName, topicLink);

        try {
            jakarta.mail.internet.MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmails.toArray(new String[0]));
            helper.setSubject(subject);
            helper.setText(emailContent, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Async
    private String loadNewTopicEmailTemplate(String topicTitle, String categoryName, String topicLink) {
        Context context = new Context();
        context.setVariable("topicTitle", topicTitle);
        context.setVariable("categoryName", categoryName);
        context.setVariable("topicLink", topicLink);
        return templateEngine.process("new-topic-email", context);
    }

    @Async
    public void sendNewCommentEmail(List<String> toEmails, String subject, String topicTitle, String categoryName, String firstName, String lastName, String commentContent, String topicLink) {
        // Load the Thymeleaf email template
        String emailContent = loadCommentEmailTemplate(topicTitle, categoryName, firstName, lastName, commentContent, topicLink);

        try {
            jakarta.mail.internet.MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmails.toArray(new String[0]));
            helper.setSubject(subject);
            helper.setText(emailContent, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
           
            e.printStackTrace();
        }
    }
    @Async
    private String loadCommentEmailTemplate(String topicTitle, String categoryName, String firstName, String lastName, String commentContent, String topicLink) {
        Context context = new Context();
        context.setVariable("topicTitle", topicTitle);
        context.setVariable("categoryName", categoryName);
        context.setVariable("firstName", firstName);
        context.setVariable("lastName", lastName);
        context.setVariable("commentContent", commentContent);
        context.setVariable("topicLink", topicLink);
        return templateEngine.process("new-comment-email", context);
    }
    
    @Async
    public void sendVerificationEmail(String to, String subject, String htmlContent) {
    	jakarta.mail.internet.MimeMessage message = javaMailSender.createMimeMessage();
        

        try {
        	MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setText(htmlContent, true); 
            helper.setTo(to);
            helper.setSubject(subject);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            
            e.printStackTrace();
        }
    }
    
    public void sendContactEmail(ContactForm contactForm) {
    	jakarta.mail.internet.MimeMessage message = javaMailSender.createMimeMessage();
        try {
        	MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo("afrpheocnet@gmail.com");
            helper.setSubject(contactForm.getSubject());
            helper.setText("From: " + contactForm.getName() + " (" + contactForm.getEmail() + ")\n\n"
                    + contactForm.getMessage());
        } catch (MessagingException e) {
            e.printStackTrace();
            // Handle exception
        }
        javaMailSender.send(message);
    }
}
