package net.pheocnetafr.africapheocnet.exception;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        // Customize your error messages based on exception types
        if (exception instanceof DisabledException) {
            // Handle disabled user account
            response.sendRedirect("/login?error=disabled");
        } else if (exception instanceof LockedException) {
            // Handle locked user account
            response.sendRedirect("/login?error=locked");
        } else {
            // Handle other authentication failures
            response.sendRedirect("/login?error=invalid");
        }
    }
}



