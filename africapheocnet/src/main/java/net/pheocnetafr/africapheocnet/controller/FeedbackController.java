package net.pheocnetafr.africapheocnet.controller;

import net.pheocnetafr.africapheocnet.entity.Feedback;
import net.pheocnetafr.africapheocnet.entity.Member;
import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.repository.FeedbackRepository;
import net.pheocnetafr.africapheocnet.repository.MemberRepository;
import net.pheocnetafr.africapheocnet.repository.UserRepository;
import net.pheocnetafr.africapheocnet.service.EmailService;
import net.pheocnetafr.africapheocnet.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {
	
	private final UserRepository userRepository;
    private final FeedbackService feedbackService;
    

    @Autowired
    public FeedbackController(UserRepository userRepository, FeedbackRepository feedbackRepository, FeedbackService feedbackService) {
        this.userRepository = userRepository;
        this.feedbackRepository = feedbackRepository;
        this.feedbackService = feedbackService;
    }

    
    @Autowired
    private EmailService emailService; 
    
    @Autowired
    private FeedbackRepository feedbackRepository;
    
    @Autowired
    private MemberRepository memberRepository;

    
    
    @PostMapping("/new")
    public ResponseEntity<String> createFeedback(@RequestBody Feedback feedback) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName(); 

            User user = userRepository.findByEmail(userEmail); 

            feedback.setUserEmail(userEmail);
            feedback.setFirstName(user.getFirstName()); 
            feedback.setSubmissionDate(new Date()); 
            feedback.setFeedbackText(feedback.getFeedbackText());

            feedbackRepository.save(feedback);
            return ResponseEntity.ok("Feedback submitted successfully.");
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving feedback: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
        }
    }




    @GetMapping("/list")
    public String listFeedback(Model model,Principal principal) {
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
         
        List<Feedback> feedbackList = feedbackService.getAllFeedback();
        model.addAttribute("feedbackList", feedbackList);
        return "feedback-list";
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
    @GetMapping("/listNewFeedback")
    public String listNewFeedback(Model model, Principal principal) {
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
         
        List<Feedback> newFeedbackList = feedbackService.getFeedbackByStatus("New");
        model.addAttribute("newFeedbackList", newFeedbackList);
        model.addAttribute("pageTitle", "Feedback List | Africa PHEOC-Net");
        return "feedback-list";
    }
    
    @GetMapping("/cleared")
    public String listClearFeedback(Model model, Principal principal) {
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
         
        List<Feedback> newFeedbackList = feedbackService.getFeedbackByStatus("Cleared");
        model.addAttribute("newFeedbackList", newFeedbackList);
        return "cleared-feedback-list";
    }


    @GetMapping("/view/{id}")
    public String viewFeedback(@PathVariable Long id, Model model,Principal principal) {
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
         
        Feedback feedback = feedbackService.getFeedbackById(id).orElse(null);
        if (feedback != null) {
            model.addAttribute("feedback", feedback);
            return "feedback-view";
        }
        return "redirect:/feedback/list";
    }

    @GetMapping("/filter/{status}")
    public String filterFeedbackByStatus(@PathVariable String status, Model model) {
        List<Feedback> feedbackList = feedbackService.getFeedbackByStatus(status);
        model.addAttribute("feedbackList", feedbackList);
        return "feedback/list";
    }

    @PostMapping("/clear/{id}")
    public String clearFeedback(@PathVariable Long id) {
        feedbackService.updateFeedbackStatus(id, "Cleared");
        return "redirect:/feedback/list";
    }

    @PostMapping("/contact/{id}")
    public String contactUser(@PathVariable Long id, @RequestParam String email, @RequestParam String subject, @RequestParam String content) {
        // Create a model for the email template
        Map<String, Object> emailModel = new HashMap<>();
        emailModel.put("subject", subject);
        emailModel.put("content", content);

        emailService.sendEmail(email, "contact-user-email.html", emailModel); 

       
        return "redirect:/feedback/list";
    }

    
}
