package net.pheocnetafr.africapheocnet.controller;

import java.security.Principal;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import net.pheocnetafr.africapheocnet.service.NotificationService;
import net.pheocnetafr.africapheocnet.service.RoleService;
import net.pheocnetafr.africapheocnet.service.UserService;
import net.pheocnetafr.africapheocnet.entity.Member;
import net.pheocnetafr.africapheocnet.entity.Notification;
import net.pheocnetafr.africapheocnet.entity.Role;
import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.repository.MemberRepository;
import net.pheocnetafr.africapheocnet.repository.UserRepository;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private MemberRepository memberRepository;  // Added MemberRepository

    @Autowired
    private UserRepository userRepository;  // Added UserRepository

    @GetMapping("/users")
    public String showAdminUsers(Model model, Principal principal) {
        // Fetch all users
        List<User> allUsers = userService.getAllUsers();

        // Fetch the authenticated user
        String username = principal.getName();
        User user = userRepository.findByEmail(username);

        // Add user's information to the model if available
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

        // Add attributes to the model
        model.addAttribute("allUsers", allUsers);
        model.addAttribute("pageTitle", "User Management | Africa PHEOC-Net");

        return "admin-users";
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

    // Create a new role (only accessible to users with the "ADMIN" role)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/roles/create")
    public String createRole(@ModelAttribute Role role) {
        roleService.createRole(role);
        return "redirect:/admin/roles";
    }

    // Update an existing role (only accessible to users with the "ADMIN" role)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/roles/{roleId}/edit")
    public String updateRole(@PathVariable Long roleId, @ModelAttribute Role updatedRole) {
        Role role = roleService.updateRole(roleId, updatedRole);
        if (role != null) {
            return "redirect:/admin/roles/" + roleId;
        } else {
            return "redirect:/admin/roles";
        }
    }
}
