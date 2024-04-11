package net.pheocnetafr.africapheocnet.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Connection;


@Controller
public class AuthSignoutController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/auth-signout")
    public String authSignout(HttpServletRequest request, HttpServletResponse response) {
        // Invalidate session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Clear authentication details
        SecurityContextHolder.clearContext();

        // Clear cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }

        // Close database connection
        try {
            Connection connection = dataSource.getConnection();
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            // Handle SQLException
            e.printStackTrace();
        }

        // Perform logout using SecurityContextLogoutHandler
        new SecurityContextLogoutHandler().logout(request, response, null);

        // Redirect to the auth-signout template after successful logout
        return "index";
    }
}
