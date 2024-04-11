package net.pheocnetafr.africapheocnet.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.pheocnetafr.africapheocnet.entity.Application;
import net.pheocnetafr.africapheocnet.entity.Member;
import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.repository.ApplicationRepository;
import net.pheocnetafr.africapheocnet.repository.MemberRepository;
import net.pheocnetafr.africapheocnet.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public String generateResetToken() {
        // Generate a unique reset token here, such as using UUID
        String resetToken = UUID.randomUUID().toString();
        return resetToken;
    }
  
    public String submitApplication(Application application) {
        // Check if a user with the same email exists in tbl_user
        User existingUser = userRepository.findByEmail(application.getEmail());
        if (existingUser != null) {
            return "User with this email already exists.";
        }

        // Check if a pending application with the same email exists in tbl_application
        Application existingApplication = applicationRepository.findByEmailAndStatus(application.getEmail(), "pending");
        if (existingApplication != null) {
            return "An application with the same email is pending for approval.";
        }

        // Save the application in tbl_application
        
        application.setStatus("Pending");
        Application savedApplication = applicationRepository.save(application);

        // Send an email to notify the user of successful application submission
        emailService.sendApplicationSubmittedEmail(savedApplication.getEmail(),savedApplication.getFirstName());

        return "Application submitted successfully!";
    }



    public void approveApplication(long applicationId) {
        // Find the application by ID 
        Application application = applicationRepository.findById(applicationId).orElse(null);
        if (application != null) {
            

        	// Create a new member profile in tbl_member
        	Member member = new Member();
        	member.setFirstName(application.getFirstName());
        	member.setLastName(application.getLastName());
        	member.setEmail(application.getEmail());
        	member.setGender(application.getGender());
        	member.setNationality(application.getNationality());
        	member.setProfession(application.getProfession());
        	member.setOrganization(application.getOrganization());
        	member.setPosition(application.getPosition());
        	member.setExpertise(application.getAreaExpertise());
        	member.setLanguage(application.getLanguage());
        	member.setEnrollment(LocalDate.now()); 
        	member.setBio(""); 

            Member savedMember = memberRepository.save(member);

            // Create a new user in tbl_user for login purposes
            User user = new User();
            user.setEmail(application.getEmail());

            // Generate a random password (encrypted with bcrypt)
            String randomPassword = generateRandomPassword();
            String hashedPassword = passwordEncoder.encode(randomPassword);
            
            // Generate a reset token 
            String resetToken = generateResetToken();

            // Set reset token and other fields
            user.setPassword(hashedPassword);
            user.setRole("USER");
            user.setLastName(application.getLastName());
            user.setFirstName(application.getFirstName());
            user.setResetToken(resetToken);
            user.setMfaEnabled(true);

            // Set reset token expiration 30 minutes after the saved time
            LocalDateTime resetTokenExpiration = LocalDateTime.now().plusMinutes(30);
            user.setResetTokenExpiration(resetTokenExpiration);

            user.setEnabled(true); 
            User savedUser = userRepository.save(user);

            
            // Notify the user of the approval
            emailService.sendApplicationApprovedEmail(savedUser.getEmail(),savedUser.getFirstName(),resetToken);
            
            // Delete the application from tbl_application
            applicationRepository.delete(application);
        }
    }

    private String generateRandomPassword() {
        // Generate a unique password
        return UUID.randomUUID().toString();
    }

    public void rejectApplication(long applicationId) {
        // Find the application by ID and set its status to "rejected"
        Application application = applicationRepository.findById(applicationId).orElse(null);
        if (application != null) {
            // Delete the application from tbl_application
            applicationRepository.delete(application);

            // Send an email to notify the user of application rejection
            emailService.sendApplicationRejectedEmail(application.getEmail(),application.getFirstName());
        }
    }

    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }
    
    public Application getApplicationById(Long id) {
        Optional<Application> optionalApplication = applicationRepository.findById(id);
        return optionalApplication.orElse(null);
    }
}
