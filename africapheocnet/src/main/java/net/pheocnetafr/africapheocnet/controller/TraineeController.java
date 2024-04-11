package net.pheocnetafr.africapheocnet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import net.pheocnetafr.africapheocnet.CountryList;
import net.pheocnetafr.africapheocnet.util.AutoFilledData;
import net.pheocnetafr.africapheocnet.entity.Member;
import net.pheocnetafr.africapheocnet.entity.TraineeRegistry;
import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.repository.MemberRepository;
import net.pheocnetafr.africapheocnet.repository.UserRepository;
import net.pheocnetafr.africapheocnet.service.TraineeService;

import java.util.List;
import java.security.Principal;
import java.util.Base64;
import java.util.Date;

@Controller
@RequestMapping("/trainees")
public class TraineeController {

    @Autowired
    private TraineeService traineeService;
    
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;
    
    @ModelAttribute("countries")
    public List<String> getAllCountries() {
        return AutoFilledData.getAllAfricanCountries();
    }
    
    @ModelAttribute("roles")
    public List<String> getTraineeRoleList() {
        return AutoFilledData.getTraineeRoleList();
    }

    @ModelAttribute("Module")
    public List<String> getModuleList() {
        return AutoFilledData.getModuleList();
    }
 
    @PostMapping("/add")
    public ResponseEntity<String> addTrainee(@ModelAttribute TraineeRegistry trainee) {
        try {
           
            Date trainingDate = trainee.getTrainingDate();
            trainee.setTrainingDate(trainingDate);
            
            traineeService.addTrainee(trainee);
            
            return ResponseEntity.ok("Trainee added successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add trainee: " + e.getMessage());
        }
    }

    // Delete a trainee
    @GetMapping("/delete/{id}")
    public String deleteTrainee(@PathVariable Long id) {
        traineeService.deleteTrainee(id);
        return "redirect:/trainees/list";
    }

    // List all trainees
    @GetMapping("/list")
    public String listTrainees(Model model,Principal principal) {
    	addUserInfoToModel(model, principal);
        List<TraineeRegistry> trainees = traineeService.getAllTrainees();
        model.addAttribute("trainees", trainees);
        model.addAttribute("pageTitle", "In-country training | Africa PHEOC-Net");
        return "trainee-list";
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<String> updateTrainee(@PathVariable Long id, @ModelAttribute TraineeRegistry trainee) {
        TraineeRegistry existingTrainee = traineeService.getTraineeById(id);
        if (existingTrainee != null) {
            try {
                // Update the existing trainee with the data received from the client side
                existingTrainee.setFirstName(trainee.getFirstName());
                existingTrainee.setLastName(trainee.getLastName());
                existingTrainee.setEmail(trainee.getEmail());
                existingTrainee.setPhoneNumber(trainee.getPhoneNumber());
                existingTrainee.setOrganization(trainee.getOrganization());
                existingTrainee.setPosition(trainee.getPosition());
                existingTrainee.setModuleAttended(trainee.getModuleAttended());
                existingTrainee.setTrainingDate(trainee.getTrainingDate());
                existingTrainee.setCountry(trainee.getCountry());
                existingTrainee.setRole(trainee.getRole());

                // Save the updated trainee
                traineeService.updateTrainee(id, existingTrainee);
                
                return ResponseEntity.ok("Trainee updated successfully");
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update trainee: " + e.getMessage());
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trainee with ID " + id + " not found");
        }
    }
    
    @GetMapping("/edit/{id}")
    public String showEditTraineeForm(@PathVariable Long id, Model model,Principal principal) {
    	addUserInfoToModel(model, principal);
        TraineeRegistry trainee = traineeService.getTraineeById(id);
        if (trainee == null) {
            
            return "error"; 
        }
        model.addAttribute("trainee", trainee);
        model.addAttribute("countries", getAllCountries());
        model.addAttribute("roles", getTraineeRoleList());
        model.addAttribute("Module", getModuleList());
        model.addAttribute("pageTitle", "In-country training | Africa PHEOC-Net");
        return "edit-trainee"; 
    }
    
    private void addUserInfoToModel(Model model, Principal principal) {
        if (principal != null) {
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
        }
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
}

