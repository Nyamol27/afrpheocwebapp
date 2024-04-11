package net.pheocnetafr.africapheocnet.security;

import net.pheocnetafr.africapheocnet.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final CustomUserDetailsService customUserDetailsService;
    private final MfaService mfaService;

    @Autowired
    public CustomAuthenticationSuccessHandler(CustomUserDetailsService customUserDetailsService,
                                              MfaService mfaService) {
        this.customUserDetailsService = customUserDetailsService;
        this.mfaService = mfaService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        if (userDetails != null) {
            User user = customUserDetailsService.loadUserByEmail(userDetails.getUsername());

            if (user != null) {
                if (user.isMfaEnabled()) {
                    // MFA enabled, redirect to verification page
                    String email = userDetails.getUsername();
                    if (mfaService.sendVerificationCodeByEmail(email)) {
                        // Verification code sent successfully, redirect to verification page
                        response.sendRedirect(request.getContextPath() + "/verify-code?email=" + email);
                    } else {
                        // Unable to send verification code, redirect to error page
                        response.sendRedirect(request.getContextPath() + "/error");
                    }
                } else {
                    // MFA not enabled, set the session and redirect to the appropriate dashboard
                    HttpSession session = request.getSession();
                    setSession(userDetails, session);
                    redirectToDashboard(response, userDetails);
                }
            } else {
                // User not found, redirect to error page
                response.sendRedirect(request.getContextPath() + "/error");
            }
        } else {
            // Invalid user, redirect to error page
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }


    private void setSession(UserDetails userDetails, HttpSession session) {
        // Set session attributes as needed, e.g., user details, roles, etc.
        session.setAttribute("userDetails", userDetails);
    }

    private void redirectToDashboard(HttpServletResponse response, UserDetails userDetails) throws IOException {
        // Redirect to the appropriate dashboard based on the user's role
        if (userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ADMIN"))) {
            response.sendRedirect("/admin_dashboard");
        } else if (userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("TRAINER"))) {
            response.sendRedirect("/trainer_dashboard");
        } else {
            response.sendRedirect("/user_dashboard");
        }
    }
}
