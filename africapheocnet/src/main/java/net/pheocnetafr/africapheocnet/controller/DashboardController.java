package net.pheocnetafr.africapheocnet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import net.pheocnetafr.africapheocnet.model.DashboardModel;
import net.pheocnetafr.africapheocnet.model.User;
import net.pheocnetafr.africapheocnet.repository.UserRepository;
import java.util.Optional;

@Controller
public class DashboardController {

    @Autowired
    private UserRepository userRepository; // Assuming you have a UserRepository

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        // Get authentication details
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        // Get user details from the database
        net.pheocnetafr.africapheocnet.entity.User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }



        // Create DashboardModel
        DashboardModel dashboardModel = new DashboardModel();
        dashboardModel.setUsername(username);  
        dashboardModel.setRole(role);
        // Set other fields as needed

        // Add DashboardModel to the model
        model.addAttribute("dashboardModel", dashboardModel);

        // Return the view name
        return "dashboard";
    }
}
