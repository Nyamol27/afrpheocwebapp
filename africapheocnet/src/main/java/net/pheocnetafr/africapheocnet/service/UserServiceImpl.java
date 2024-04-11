package net.pheocnetafr.africapheocnet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import net.pheocnetafr.africapheocnet.entity.Application;
import net.pheocnetafr.africapheocnet.entity.Member;
import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.repository.ApplicationRepository;
import net.pheocnetafr.africapheocnet.repository.MemberRepository;
import net.pheocnetafr.africapheocnet.repository.UserRepository;
import net.pheocnetafr.africapheocnet.util.TokenGenerator;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    // Duration for which the reset link is valid (24 hours in this example)
    private static final long RESET_LINK_EXPIRATION_HOURS = 24;

    @Override
    public User registerUser(User user) {
        validateRegistration(user);
        user.setRole("ROLE_USER");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        return userRepository.save(user);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void changePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        }
    }

    @Override
    public void approveApplication(Long applicationId) {
        Optional<Application> optionalApplication = applicationRepository.findById(applicationId);

        if (optionalApplication.isPresent()) {
            Application application = optionalApplication.get();

            Member member = new Member();
            member.setFirstName(application.getFirstName());
            member.setLastName(application.getLastName());
            member.setEmail(application.getEmail());
            member.setGender(application.getGender());
            member.setNationality(application.getNationality());
            member.setSector(application.getSector());
            member.setOrganization(application.getOrganization());
            member.setPosition(application.getPosition());
            member.setExpertise(application.getAreaExpertise());
            member.setLanguage(application.getLanguage());
            // Set other properties...
            member.setEnrolement(LocalDateTime.now().toString()); // Set to the current date and time

            memberRepository.save(member);

            User user = new User();
            user.setEmail(member.getEmail());
            user.setPassword(passwordEncoder.encode(TokenGenerator.generateRandomToken())); // Generate a random password
            user.setFirstName(member.getFirstName());
            user.setLastName(member.getLastName());
            user.setRole("ROLE_USER");
            user.setEnabled(true);

            userRepository.save(user);

            // Send welcome email to the user with a link to reset password
            emailService.sendPasswordResetLink(member.getEmail(), TokenGenerator.generateRandomToken());

            // Delete the application
            applicationRepository.delete(application);
        } else {
            throw new IllegalArgumentException("Application not found");
        }
    }
    
    @Override
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email);
    }

    @Override
    public void sendPasswordResetLink(String email) {
        User user = userRepository.findByEmail(email);

        if (user != null) {
            // Generate a reset token and set an expiration timestamp
            String resetToken = TokenGenerator.generateRandomToken();
            user.setResetToken(resetToken);
            user.setResetTokenExpiration(LocalDateTime.now().plusHours(RESET_LINK_EXPIRATION_HOURS));
            userRepository.save(user);

            emailService.sendPasswordResetLink(email, resetToken);
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }

    @Override
    public void resetPassword(String email, String newPassword, String resetToken) {
        User user = userRepository.findByEmailAndResetToken(email, resetToken);

        if (user != null && isResetTokenValid(user)) {
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setResetToken(null);
            user.setResetTokenExpiration(null);
            userRepository.save(user);
        } else {
            throw new IllegalArgumentException("Invalid reset token or expired link");
        }
    }
    
    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Check if the reset token is still valid based on the expiration timestamp
    private boolean isResetTokenValid(User user) {
        LocalDateTime expirationTime = user.getResetTokenExpiration();
        return expirationTime != null && LocalDateTime.now().isBefore(expirationTime);
    }

    private void validateRegistration(User user) {
        User existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser != null) {
            throw new IllegalArgumentException("Email address is already in use");
        }
    }
}
