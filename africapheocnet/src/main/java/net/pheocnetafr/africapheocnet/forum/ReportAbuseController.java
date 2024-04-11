package net.pheocnetafr.africapheocnet.forum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.repository.UserRepository;

import java.sql.Timestamp;

import org.springframework.stereotype.Controller;

@Controller 
@RequestMapping("/api/comments")
public class ReportAbuseController {

    @Autowired
    private ReportAbuseRepository reportAbuseRepository;

    @Autowired
    private UserRepository userRepository; 

    @PostMapping("/report-abuse")
    public ResponseEntity<String> reportAbuse(
            @RequestParam Long commentId
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        String userEmail = authentication.getName();
        User reportingUser = userRepository.findByEmail(userEmail); 
        if (reportingUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // Create a ReportAbuse object
        ReportAbuse reportAbuse = new ReportAbuse();
        reportAbuse.setCommentId(commentId);
        reportAbuse.setReportingUser(reportingUser);
        reportAbuse.setReportDate(new Timestamp(System.currentTimeMillis()));
        reportAbuse.setStatus("submitted"); // Default status

        // Save the report to the database
        reportAbuseRepository.save(reportAbuse);

        return ResponseEntity.ok("Abuse reported successfully");
    }
}
