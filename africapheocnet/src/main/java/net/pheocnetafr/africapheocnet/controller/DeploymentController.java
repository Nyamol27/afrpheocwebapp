package net.pheocnetafr.africapheocnet.controller;

import java.security.Principal;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import net.pheocnetafr.africapheocnet.entity.Deployment;
import net.pheocnetafr.africapheocnet.entity.Member;
import net.pheocnetafr.africapheocnet.entity.Trainer;
import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.repository.MemberRepository;
import net.pheocnetafr.africapheocnet.repository.TrainerRepository;
import net.pheocnetafr.africapheocnet.repository.UserRepository;
import net.pheocnetafr.africapheocnet.service.DeploymentService;

@Controller
@RequestMapping("/deployments")
public class DeploymentController {
    
    private final TrainerRepository trainerRepository;
    private final DeploymentService deploymentService;
    
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public DeploymentController(TrainerRepository trainerRepository, DeploymentService deploymentService) {
        this.trainerRepository = trainerRepository;
        this.deploymentService = deploymentService;
    }

    // Display a list of all deployments
    @GetMapping("/list")
    public String listDeployments(Model model,Principal principal) {
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
         
        List<Deployment> deployments = deploymentService.getAllDeployments();
        model.addAttribute("deployments", deployments);
        return "deployment/list"; 
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
    // Display the form for adding a new deployment
    @GetMapping("/add")
    public String showAddDeploymentForm(Model model) {
        // Initialize a new Deployment object
        Deployment deployment = new Deployment();
        model.addAttribute("deployment", deployment);
        return "deployment/add"; 
    }

    @PostMapping("/add/{trainerId}")
    public ResponseEntity<String> addDeployment(@PathVariable Long trainerId, @RequestBody Deployment deployment) {
        try {
            // Check if the trainer exists in the database
            Optional<Trainer> existingTrainer = trainerRepository.findById(trainerId);
            if (!existingTrainer.isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Trainer with ID " + trainerId + " does not exist!");
            }

            // Set the trainer for the deployment
            deployment.setTrainer(existingTrainer.get());

            // Add the deployment
            deploymentService.createDeployment(deployment);
            return ResponseEntity.ok("Deployment added successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding deployment: " + e.getMessage());
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditDeploymentForm(@PathVariable Long id, Model model, Principal principal) {
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
        Deployment deployment = deploymentService.getDeploymentById(id);

        if (deployment != null) {
            model.addAttribute("deployment", deployment);
            model.addAttribute("pageTitle", "Edit Deployment | Africa PHEOC-Net");
            return "deployment-edit";
        } else {
            // Log warning if deployment not found
            return "deployment-not-found"; 
        }
    }

    // Handle form submission for updating an existing deployment
    @PostMapping("/update/{id}")
    public ResponseEntity<String> updateDeploymentAjax(@PathVariable Long id, @RequestBody Deployment updatedDeployment) {
        try {
            deploymentService.updateDeployment(id, updatedDeployment);
            return ResponseEntity.ok("Deployment updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating deployment: " + e.getMessage());
        }
    }

    // Handle deletion of a deployment
    @PostMapping("/delete/{id}")
    public String deleteDeployment(@PathVariable Long id) {
        // Add logic to delete the deployment with the given ID
        deploymentService.deleteDeployment(id);
        return "redirect:/deployments/list"; // Redirect to the list of deployments after deleting
    }
}
