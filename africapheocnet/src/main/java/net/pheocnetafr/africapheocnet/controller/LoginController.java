package net.pheocnetafr.africapheocnet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LoginController {

	@GetMapping("/login")
    public String showLoginForm() {
        return "index"; 
    }

    @GetMapping("/dashboard")
    public String showDashboard() {
        if (userHasRole("ROLE_ADMIN")) {
            return "admin/dashboard"; // Redirect to the admin dashboard
        } else if (userHasRole("ROLE_USER")) {
            return "user/dashboard"; // Redirect to the user dashboard
        } else if (userHasRole("ROLE_TRAINER")) {
            return "trainer/dashboard"; // Redirect to the trainer dashboard
        } else {
            return "access-denied"; // Redirect to an access-denied page for roles not specified
        }
    }
  
    private boolean userHasRole(String role) {
        // Get the authentication object from the security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check if the user is authenticated
        if (authentication != null && authentication.isAuthenticated()) {
            // Check if the user has the specified role
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if (authority.getAuthority().equals(role)) {
                    return true; // User has the specified role
                }
            }
        }

        return false; // User does not have the specified role
    }
}

