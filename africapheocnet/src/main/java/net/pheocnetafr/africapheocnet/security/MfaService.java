package net.pheocnetafr.africapheocnet.security;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.repository.UserRepository;
import net.pheocnetafr.africapheocnet.service.EmailService;

@Service
public class MfaService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpringTemplateEngine templateEngine;

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 1000 + random.nextInt(9000); 
        return String.valueOf(code);
    }

    public boolean sendVerificationCodeByEmail(String email) {
    	User user = userRepository.findByEmail(email);

        if (user != null) {
            String code = generateVerificationCode();
            user.setVerificationCode(code);
            user.setVerificationCodeExpiration(LocalDateTime.now().plusMinutes(3)); // Assuming a 3-minute expiration
            user.setMfaEnabled(true); // Enable MFA for the user

            try {
                userRepository.save(user); // Save the user with verification code and expiration
            } catch (Exception e) {
                e.printStackTrace(); // Handle the exception appropriately
                return false; // Unable to save user with verification code
            }
           
            // Prepare the email content with the generated code
            Context context = new Context();
            context.setVariable("firstName", user.getFirstName());
            context.setVariable("code", code);

            String htmlContent = templateEngine.process("verification_email", context);

            String subject = "Africa PHEOC-Net : Verification Code";
            emailService.sendVerificationEmail(email, subject, htmlContent);

            return true; // Verification code sent successfully
        }

        return false; // User not found or unable to send the verification code
    }


    public boolean verifyVerificationCode(User user, String code) {
        if (user != null && user.isMfaEnabled() && code.equals(user.getVerificationCode())) {
            LocalDateTime expirationTime = user.getVerificationCodeExpiration();
            if (expirationTime != null && LocalDateTime.now().isBefore(expirationTime)) {
                return true; 
            }
        }
        return false; 
    }

}
