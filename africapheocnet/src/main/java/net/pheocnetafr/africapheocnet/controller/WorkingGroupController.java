package net.pheocnetafr.africapheocnet.controller;

import net.pheocnetafr.africapheocnet.entity.Member;
import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.entity.WorkingGroup;
import net.pheocnetafr.africapheocnet.repository.MemberRepository;
import net.pheocnetafr.africapheocnet.repository.UserRepository;
import net.pheocnetafr.africapheocnet.service.WorkingGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/working-groups")
public class WorkingGroupController {
    private final WorkingGroupService workingGroupService;
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public WorkingGroupController(WorkingGroupService workingGroupService) {
        this.workingGroupService = workingGroupService;
    }

    @GetMapping
    public String getAllWorkingGroups(Model model, Principal principal) {
    	addUserInfoToModel(model, principal);
        List<WorkingGroup> workingGroups = workingGroupService.getAllWorkingGroups();
        model.addAttribute("workingGroups", workingGroups);
        return "working-groups/index";
    }

    @GetMapping("/{id}")
    public String getWorkingGroupById(@PathVariable Long id, Model model,Principal principal) {
    	addUserInfoToModel(model, principal);
        WorkingGroup workingGroup = workingGroupService.getWorkingGroupById(id).orElse(null);
        model.addAttribute("workingGroup", workingGroup);
        return "working-groups/details"; 
    }

    @GetMapping("/new")
    public String createWorkingGroupForm(Model model, Principal principal) {
    	addUserInfoToModel(model, principal);
        model.addAttribute("workingGroup", new WorkingGroup());
        return "working-groups/create"; 
    }

    @PostMapping("/new")
    public String createWorkingGroup(@ModelAttribute WorkingGroup workingGroup, BindingResult bindingResult) {
    	
        if (bindingResult.hasErrors()) {
            return "working-groups/create"; 
        }
        workingGroupService.createWorkingGroup(workingGroup);
        return "redirect:/working-groups";
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkingGroup(@PathVariable Long id) {
        workingGroupService.deleteWorkingGroup(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
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
