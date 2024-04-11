package net.pheocnetafr.africapheocnet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import net.pheocnetafr.africapheocnet.entity.Member;
import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.repository.UserRepository;
import net.pheocnetafr.africapheocnet.util.UserNotFoundException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    @Autowired
    private TemplateEngine templateEngine; 

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public User registerUser(String email, String password) {
        User existingUser = userRepository.findByEmail(email);
        if (existingUser != null) {
            throw new UserAlreadyExistsException("User with this email already exists.");
        }

        String encodedPassword = passwordEncoder.encode(password);

        User user = new User();
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setRole("standard user");
        user.setEnabled(true);

        return userRepository.save(user);
    }

    public User initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            String resetToken = UUID.randomUUID().toString();
            
            // Set token expiration time (e.g., 24 hours from now)
            LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(2);
            
            user.setResetToken(resetToken);
            user.setResetTokenExpiration(expirationTime); 
            userRepository.save(user);
            sendPasswordResetEmail(user.getFirstName(), user.getEmail(), resetToken);
        }
        return user;
    }


    @Transactional
    public User resetPassword(String email, String resetToken, String newPassword) {
        User user = userRepository.findByEmailAndResetToken(email, resetToken);
        if (user != null) {
            String encodedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedPassword);
            user.setResetToken(null);
            userRepository.save(user);
            sendPasswordResetConfirmationEmail(user.getEmail());
        }
        return user;
    }

    public User updatePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            String encodedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedPassword);
            userRepository.save(user);
        }
        return user;
    }

    @Transactional
    public User changeUserStatus(Long userId, boolean status) {
        // Find the user by userId
        User user = userRepository.findById(userId).orElse(null);

        if (user != null) {
            // Update the user's status
            user.setEnabled(status);
            userRepository.save(user);
        }

        return user;
    }

    
    public User updateUserRole(String userEmail, String newRole) {
        User user = userRepository.findByEmail(userEmail);
        if (user != null) {
            user.setRole(newRole);
            userRepository.save(user);
            return user;
        }
        return null;
    }

    private void sendPasswordResetEmail(String firstName, String userEmail, String resetToken) {
        emailService.sendPasswordResetEmail(firstName, userEmail, resetToken);
    }

    private void sendPasswordResetConfirmationEmail(String userEmail) {
        emailService.sendPasswordResetConfirmationEmail(userEmail);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            userRepository.delete(user);
        }
    }

    @Transactional
    public void updateUserPassword(User user, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Exception class for user already exists scenario
    public static class UserAlreadyExistsException extends RuntimeException {
        public UserAlreadyExistsException(String message) {
            super(message);
        }
    }
    
    
    public User lockUser(String userId) {
        Optional<User> optionalUser = userRepository.findById(Long.valueOf(userId));
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setEnabled(false);
            userRepository.save(user);
            return user;
        }
        return null; 
    }

    public User unlockUser(String userId) {
        Optional<User> optionalUser = userRepository.findById(Long.valueOf(userId));
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setEnabled(true);
            userRepository.save(user);
            return user;
        }
        return null; 
    }

    public Optional<User> findById(String userId) {
        return userRepository.findById(Long.valueOf(userId));
    }
    
    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }
    
    public User registerNewUser(String firstName, String lastName, String email) {
        // Check if the user already exists
        User existingUser = userRepository.findByEmail(email);
        if (existingUser != null) {
            // User already exists, throw an exception or return a specific value
            throw new UserAlreadyExistsException("User with email " + email + " already exists");
        }

        // User does not exist, create a new user
        User newUser = new User();
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setEmail(email);
        newUser.setEnabled(true); 
        newUser.setRole("ADMIN");
       
        User savedUser = userRepository.save(newUser);

        // Send an email to the user to notify them that an account has been created
        sendAccountCreationEmail(savedUser);

        return savedUser;
    }

 // Method to send an email to the user to notify them that an account has been created
    private void sendAccountCreationEmail(User user) {
        try {
            // Generate a unique reset token
            String resetToken = UUID.randomUUID().toString();

            // Set the reset token for the user
            user.setResetToken(resetToken);
            userRepository.save(user);

            // Prepare template variables
            Context context = new Context();
            context.setVariable("firstName", user.getFirstName());
            context.setVariable("lastName", user.getLastName());
            context.setVariable("resetToken", resetToken);

            // Process the Thymeleaf template
            String emailContent = templateEngine.process("account-creation-email", context);

            // Send the email
            emailService.sendAccountCreationEmail(user, "Account Created - Password Reset", emailContent);
        } catch (Exception e) {
            // Handle exception
            e.printStackTrace(); 
        }
    }

    public void updateUserProfile(User userUpdateRequest) {
        
        User existingUser = userRepository.findByEmail(userUpdateRequest.getEmail());

        // Check if the user exists
        if (existingUser != null) {
            // Update user details with the provided values
            existingUser.setFirstName(userUpdateRequest.getFirstName());
            existingUser.setLastName(userUpdateRequest.getLastName());
            existingUser.setEmail(userUpdateRequest.getEmail()); 

            userRepository.save(existingUser);
        } else {
            
            throw new UserNotFoundException("User with email " + userUpdateRequest.getEmail() + " not found");
        }
    
}
    
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }
    
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    
    public List<User> getUsersWithRoles(String... roles) {
    	List<String> rolesList = Arrays.asList(roles);
    	return userRepository.findByRoleIn(rolesList);
    }
    
    public void deleteUserAndDependencies(User user) {
        
        // Then delete the user
        userRepository.delete(user);
    }
 

}
