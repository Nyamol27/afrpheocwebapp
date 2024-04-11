package net.pheocnetafr.africapheocnet.controller;
import java.util.Optional;

import net.pheocnetafr.africapheocnet.entity.Member;
import net.pheocnetafr.africapheocnet.entity.User;


import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.pheocnetafr.africapheocnet.repository.MemberRepository;
import net.pheocnetafr.africapheocnet.repository.UserRepository;
import net.pheocnetafr.africapheocnet.service.UserService;
import net.pheocnetafr.africapheocnet.service.UserService.UserAlreadyExistsException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

@Controller
@RequestMapping("/users")
public class UserController {

	private static final Logger logger = Logger.getLogger(UserController.class.getName());
    @Autowired
    private UserService userService;
   
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MemberRepository memberRepository;

    @GetMapping("/edit/{email}")
    public String editUserProfile(@PathVariable String email, Model model,Principal principal) {
    	addUserInfoToModel(model, principal);
        User user = userService.findByEmail(email);
        if (user != null) {
            
            model.addAttribute("user", user);
            return "edit-user-profile";
        } else {
            return "user-not-found";
        }
    }
    
  

    @GetMapping("/{email}/edit")
    public String editUserProfileForm(@PathVariable String email, Model model,Principal principal) {
    	addUserInfoToModel(model, principal);
        User user = userService.findByEmail(email);
        if (user != null) {
            model.addAttribute("user", user);
            return "edit-user-profile";
        } else {
            model.addAttribute("error", "User not found.");
            return "error-page";
        }
    }

    @PostMapping("/password-reset")
    public ResponseEntity<String> resetUserPasswordFromConfigPageByEmail(@RequestParam("email") String email, Model model, Principal principal) {
        try {
            addUserInfoToModel(model, principal);
            User user = userService.findByEmail(email);
            if (user != null) {
                // Delegate password reset to UserService
                userService.initiatePasswordReset(email);
                String message = "User password reset successfully! An email with the new password has been sent.";
                return ResponseEntity.ok(message);
            } else {
                String errorMessage = "User not found.";
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
            }
        } catch (Exception e) {
            String errorMessage = "An error occurred while resetting the user password.";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }


    @PostMapping("/delete/{email}")
    public ResponseEntity<String> deleteUserByEmail(@PathVariable String email) {
        // Check if email is provided
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email parameter is required.");
        }

        // Find the user by email
        User user = userService.findUserByEmail(email);

        // Check if the user exists
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        // Delete the user and its dependencies
        try {
            userService.deleteUserAndDependencies(user);
            return ResponseEntity.ok("User and dependencies deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete user and dependencies.");
        }
    }
    
 
    @GetMapping("/userCount")
    public String getUserCount(Model model) {
        long totalUsers = userRepository.count(); 
        model.addAttribute("totalUsers", totalUsers);
        return "userCount"; 
    }
    
    @PostMapping("/enable")
    public ResponseEntity<String> enableUser(@RequestBody Map<String, String> requestBody) {
        String userId = requestBody.get("userId");
        try {
            Optional<User> optionalUser = userService.findById(userId);
            if (optionalUser.isPresent()) {
                // Update the user's status to unlocked
                userService.unlockUser(userId);
                return ResponseEntity.ok("User unlocked successfully.");
            } else {
                return ResponseEntity.badRequest().body("User not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred while unlocking the user.");
        }
    }

    @PostMapping("/disable")
    public ResponseEntity<String> lockUser(@RequestBody Map<String, String> requestBody) {
        String userId = requestBody.get("userId");
        try {
            Optional<User> optionalUser = userService.findById(userId);
            if (optionalUser.isPresent()) {
                // Update the user's status to locked
                userService.lockUser(userId);
                return ResponseEntity.ok("User locked successfully.");
            } else {
                return ResponseEntity.badRequest().body("User not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred while locking the user.");
        }
    }




    @PostMapping("/admin/resetPassword")
    public ResponseEntity<String> resetPassword(@RequestParam String email) {
        try {
            User user = userService.findByEmail(email);
            if (user != null) {
                // Delegate password reset to UserService
                userService.initiatePasswordReset(email);
                return ResponseEntity.ok("Password reset initiated successfully. An email with the new password has been sent.");
            } else {
                return ResponseEntity.badRequest().body("User not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred while resetting the user password.");
        }
    }
    
    @PostMapping("/add")
    public ResponseEntity<String> addUser(@RequestBody User userRequest) {
        try {
            // Extract data from the request
            String firstName = userRequest.getFirstName();
            String lastName = userRequest.getLastName();
            String email = userRequest.getEmail();

            // Log the received user data
            logger.info("Received user data - First Name: " + firstName + ", Last Name: " + lastName + ", Email: " + email);

            // Save the new user
            userService.registerNewUser(firstName, lastName, email);

            return ResponseEntity.ok("User added successfully.");
        } catch (UserAlreadyExistsException e) {
            // Log the exception
            logger.warning("User with email " + userRequest.getEmail() + " already exists.");

            return ResponseEntity.badRequest().body("User with this email already exists.");
        } catch (Exception e) {
            // Log the exception
            logger.severe("Failed to add user. Exception: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add user.");
        }
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateUserProfile(@RequestBody User userUpdateRequest) {
        try {
            // Extract data from the request
            String firstName = userUpdateRequest.getFirstName();
            String lastName = userUpdateRequest.getLastName();
            String email = userUpdateRequest.getEmail();

            // Update user profile
            userService.updateUserProfile(userUpdateRequest);

            return ResponseEntity.ok("User updated successfully.");
        } catch (Exception e) {
            // Log the exception
            e.printStackTrace();

            // Return error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update user.");
        }
    }

    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model, Principal principal) {
    	addUserInfoToModel(model, principal);
        model.addAttribute("pageTitle", " Change Password | Africa PHEOC-Net");
        return "change-password-form";
    }

    
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Principal principal) {

        // Retrieve the username or email from the Principal
        String username = principal.getName();

        // Retrieve the user from the database
        User user = userRepository.findByEmail(username);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        // Check if the old password matches the one in the database
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body("Old password is incorrect");
        }

        // Check if the new password and confirm password match
        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body("New password and confirm password do not match");
        }

        

        // Hash the new password
        String hashedPassword = passwordEncoder.encode(newPassword);

        // Update the user's password
        user.setPassword(hashedPassword);
        userRepository.save(user);

        return ResponseEntity.ok("Password changed successfully");
    }

    // Method to validate the strength of the password
    private boolean isStrongPassword(String password) {
        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
        return password.matches(passwordRegex);
    }

    
    @GetMapping("/userManagement")
    public String getUserManagement(Model model, Principal principal) {
    	addUserInfoToModel(model, principal);
        List<User> users = userService.getUsersWithRoles("USER", "TRAINER");
        model.addAttribute("users", users);
        return "userManagementPage"; 
    }
    
    
    @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam("token") String token, Model model) {
        // Find the user by the reset token
        User user = userRepository.findByResetToken(token);

        if (user == null) {
            // Token not found or invalid
            model.addAttribute("type", "error");
            model.addAttribute("message", "Invalid reset token.");
            return "message"; 
        }

        // Check if the reset token has expired
        LocalDateTime resetTokenExpiration = user.getResetTokenExpiration();
        LocalDateTime currentTime = LocalDateTime.now();

        if (currentTime.isAfter(resetTokenExpiration)) {
            // Token has expired
            model.addAttribute("type", "error");
            model.addAttribute("message", "Reset token has expired.");
            return "message"; 
        }

        // Token is valid and not expired
        // Forward to the reset password page with the token
        model.addAttribute("token", token);
        return "reset-Password"; 
    }

    
    
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam("token") String token,
                                                 @RequestBody Map<String, String> requestBody,
                                                 Model model) {
        String password = requestBody.get("password");

        // Find the user by the reset token
        User user = userRepository.findByResetToken(token);

        if (user == null) {
            // Token not found or invalid
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("Invalid reset token.");
        }

        // Check if the reset token has expired
        LocalDateTime resetTokenExpiration = user.getResetTokenExpiration();
        LocalDateTime currentTime = LocalDateTime.now();

        if (currentTime.isAfter(resetTokenExpiration)) {
            // Token has expired
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("Reset token has expired.");
        }

        // Token is valid and not expired
        // Encode the new password with BCrypt
        String encodedPassword = passwordEncoder.encode(password);
        
        // Reset the user's password
        user.setPassword(encodedPassword);
        user.setResetToken(null);
        user.setResetTokenExpiration(null);
        userRepository.save(user);

        // Return a success response
        return ResponseEntity.ok("Password reset successfully.");
    }
   

    private void addUserInfoToModel(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            User user = userRepository.findByEmail(username);
            if (user != null) {
                String firstName = user.getFirstName();
                String lastName = user.getLastName();
                String initials = firstName.substring(0, 1) + lastName.substring(0, 1);
                model.addAttribute("firstName", firstName);
                model.addAttribute("lastName", lastName);
                model.addAttribute("initials", initials);
            }
            // Fetch member information including the photo
            Member member = memberRepository.findByEmail(username);
            if (member != null) {
                convertPhotoToBase64(member);
                model.addAttribute("member", member);
            }
        }
    }

    private void convertPhotoToBase64(Member member) {
        if (member != null && member.getPhoto() != null) {
            // Convert binary photo data to Base64-encoded string
            byte[] photoBytes = member.getPhoto();
            String base64Photo = Base64.getEncoder().encodeToString(photoBytes);
            // Set the Base64-encoded photo string to the member object
            member.setBase64Photo(base64Photo);
        }
    }
}
