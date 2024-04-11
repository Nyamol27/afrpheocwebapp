package net.pheocnetafr.africapheocnet.controller;

import net.pheocnetafr.africapheocnet.entity.Role;
import net.pheocnetafr.africapheocnet.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    // Display a list of all roles
    @GetMapping("/")
    public String listRoles(Model model) {
        List<Role> roles = roleService.getAllRoles();
        model.addAttribute("roles", roles);
        return "roles/list"; // Use a template to display the list of roles
    }

    // Display a form to create a new role
    @GetMapping("/create")
    public String createRoleForm(Model model) {
        model.addAttribute("role", new Role());
        return "roles/create"; // Use a template to display the create role form
    }

    // Process the form submission to create a new role
    @PostMapping("/create")
    public String createRole(@ModelAttribute Role role) {
        roleService.createRole(role);
        return "redirect:/roles/";
    }

    // Display the details of a specific role
    @GetMapping("/{roleId}")
    public String viewRole(@PathVariable Long roleId, Model model) {
        Role role = roleService.getRoleById(roleId);
        if (role != null) {
            model.addAttribute("role", role);
            return "roles/view"; // Use a template to display the role details
        } else {
            return "redirect:/roles/"; // Role not found, redirect to the list of roles
        }
    }

    // Display a form to edit an existing role
    @GetMapping("/{roleId}/edit")
    public String editRoleForm(@PathVariable Long roleId, Model model) {
        Role role = roleService.getRoleById(roleId);
        if (role != null) {
            model.addAttribute("role", role);
            return "roles/edit"; // Use a template to display the edit role form
        } else {
            return "redirect:/roles/"; // Role not found, redirect to the list of roles
        }
    }

    // Process the form submission to update an existing role
    @PostMapping("/{roleId}/edit")
    public String updateRole(@PathVariable Long roleId, @ModelAttribute Role updatedRole) {
        Role role = roleService.updateRole(roleId, updatedRole);
        if (role != null) {
            return "redirect:/roles/" + roleId;
        } else {
            return "redirect:/roles/";
        }
    }

    // Delete a role
    @GetMapping("/{roleId}/delete")
    public String deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return "redirect:/roles/";
    }
}
