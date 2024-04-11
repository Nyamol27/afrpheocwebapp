package net.pheocnetafr.africapheocnet.controller;

import net.pheocnetafr.africapheocnet.entity.Application;
import net.pheocnetafr.africapheocnet.entity.Member;
import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.repository.MemberRepository;
import net.pheocnetafr.africapheocnet.repository.UserRepository;
import net.pheocnetafr.africapheocnet.service.ApplicationService;

import java.security.Principal;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/applications")
public class ApplicationController {
	
	@Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationService applicationService;

    @GetMapping("/show")
    public String showApplicationForm(Model model) {
        model.addAttribute("application", new Application());
        return "application-form"; 
    }

    @PostMapping("/add")
    public ResponseEntity<String> addApplication(@ModelAttribute Application application,
                                                 HttpServletRequest request,
                                                 @RequestParam(name = "_csrf", required = false) String csrfToken) {
        try {
            // Submit the application
            String resultMessage = applicationService.submitApplication(application);

            // Log success message
            System.out.println("Application submitted successfully!");

            // Return success response
            return ResponseEntity.ok(resultMessage);
        } catch (RuntimeException e) {
            // Log the error message
            System.out.println("Error adding application: " + e.getMessage());

            // Return error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error adding application: " + e.getMessage());
        }
    }

    @GetMapping("/success")
    public String success(Model model) {
        return "success-page"; 
    }

    @PostMapping("/approve/{applicationId}")
    public ResponseEntity<?> approveApplication(@PathVariable long applicationId) {
        try {
            
            applicationService.approveApplication(applicationId);
            return ResponseEntity.ok("Application approved successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reject/{applicationId}")
    public ResponseEntity<?> rejectApplication(@PathVariable long applicationId) {
        try {
            
            applicationService.rejectApplication(applicationId);
            return ResponseEntity.ok("Application rejected successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/list")
    public String listApplications(Model model, Principal principal) {
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
        List<Application> applications = applicationService.getAllApplications();

        model.addAttribute("applications", applications);
        model.addAttribute("totalApplications", applications.size()); 
        model.addAttribute("pageTitle", "Applications List | Africa PHEOC-Net");
        
        return "applications-list";
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
    @GetMapping("/view/{id}")
    public String viewApplication(@PathVariable Long id, Model model, Principal principal) {
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
        Application application = applicationService.getApplicationById(id);
        model.addAttribute("apps", application);
        model.addAttribute("pageTitle", "View Application | Africa PHEOC-Net");
        return "application-view";
    }

}

