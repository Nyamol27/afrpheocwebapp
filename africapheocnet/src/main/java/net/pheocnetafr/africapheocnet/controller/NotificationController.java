package net.pheocnetafr.africapheocnet.controller;
import java.util.Optional;
import java.security.Principal;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import net.pheocnetafr.africapheocnet.entity.Member;
import net.pheocnetafr.africapheocnet.entity.Notification;
import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.repository.MemberRepository;
import net.pheocnetafr.africapheocnet.repository.UserRepository;
import net.pheocnetafr.africapheocnet.service.NotificationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

			 // Display the notification settings form
    @GetMapping("/settings")
    public String showNotificationSettingsForm(Principal principal, Model model) {
        if (principal == null) {
            // Handle the case where the user is not authenticated
            return "redirect:/login"; // Redirect to the login page or handle as needed
        }

        String email = principal.getName();
        
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

        Optional<Notification> optionalNotification = notificationService.getNotificationByEmail(email);
        boolean isEnabled = false; // Default value if no notification found
        if (optionalNotification.isPresent()) {
            isEnabled = optionalNotification.get().getIsEnable();
        }

        model.addAttribute("pageTitle", "Notification setting | Africa PHEOC-Net");
        model.addAttribute("email", email);
        model.addAttribute("isEnabled", isEnabled);

        return "notification-settings-form";
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


    	    @PostMapping("/update")
    	    public ResponseEntity<String> toggleNotificationStatus(Principal principal, @RequestParam String isEnable) {
    	        if (principal == null) {
    	            System.out.println("User not authenticated");
    	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
    	        }

    	        // Retrieve the current user's email
    	        String email = principal.getName();
    	        String firstName = ""; 

    	        

    	        try {
    	            if ("Enable".equalsIgnoreCase(isEnable)) {
    	                notificationService.enableNotificationStatus(email, firstName);
    	                
    	                return ResponseEntity.ok("Notification status updated successfully");
    	            } else if ("Disable".equalsIgnoreCase(isEnable)) {
    	                notificationService.disableNotificationStatus(email, firstName);
    	                return ResponseEntity.ok("Notification status updated successfully");
    	            } else {
    	                return ResponseEntity.badRequest().body("Invalid value for notification status");
    	            }
    	        } catch (Exception e) {
    	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    	                    .body("Failed to update notification status: " + e.getMessage());
    	        }
    	    }








}
