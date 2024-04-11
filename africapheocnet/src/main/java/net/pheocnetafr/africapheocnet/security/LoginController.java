package net.pheocnetafr.africapheocnet.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.repository.UserRepository;

@Controller
public class LoginController {

    @Autowired
    private MfaService mfaService;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/login")
    public String login(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam(name = "email") String email,
                        @RequestParam(name = "password") String password,
                        Model model,
                        HttpSession session) {
        User user = customUserDetailsService.loadUserByEmail(email);

        if (user == null || !user.isEnabled() || !customUserDetailsService.authenticate(email, password)) {
            // Authentication failed, show error message
            model.addAttribute("message", "Invalid email or password");
            return "login"; // Redirect to login page
        }

        if (user.isMfaEnabled()) {
            // MFA enabled, redirect to verification page
            return "redirect:/verify-code?email=" + email;
        } else {
            // MFA not enabled, set session and redirect to dashboard
            setSession(user, session);
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(Authentication authentication,
                                        @RequestParam(name = "code") String code) {
        // Get the username (email) from the Authentication object
        String email = authentication.getName();

        // Load user details from the database
        User user = customUserDetailsService.loadUserByEmail(email);

        if (user == null || !mfaService.verifyVerificationCode(user, code)) {
            // Code is invalid or expired, return error response
            return ResponseEntity.badRequest().body("Invalid or expired verification code");
        }

        // Verification successful, set code and expiration time to null
        user.setVerificationCode(null);
        user.setVerificationCodeExpiration(null);
        userRepository.save(user);

        // Return success response
        return ResponseEntity.ok().body("Verification successful");
    }
    

    @PostMapping("/resend-verification-code")
    public ResponseEntity<String> resendVerificationCode(Authentication authentication) {
        String email = authentication.getName();

        if (mfaService.sendVerificationCodeByEmail(email)) {
            return ResponseEntity.ok().body("Verification code resent successfully");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to resend verification code");
        }
    }

    private void setSession(User user, HttpSession session) {
        // Set session attributes as needed, e.g., user details, roles, etc.
        session.setAttribute("user", user);
    }
    
    @GetMapping("/verify-code")
    public String showVerifyCodePage(@RequestParam(name = "email") String email, Model model, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        model.addAttribute("email", email);
        return "enter_code"; 
    }
}
