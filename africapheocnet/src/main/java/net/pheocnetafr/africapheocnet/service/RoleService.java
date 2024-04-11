package net.pheocnetafr.africapheocnet.service;

import net.pheocnetafr.africapheocnet.entity.Role;
import net.pheocnetafr.africapheocnet.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRoleById(Long roleId) {
        Optional<Role> optionalRole = roleRepository.findById(roleId);
        return optionalRole.orElse(null);
    }

    public Role createRole(Role role) {
        return roleRepository.save(role);
    }

    public Role updateRole(Long roleId, Role updatedRole) {
        Optional<Role> optionalRole = roleRepository.findById(roleId);
        if (optionalRole.isPresent()) {
            Role existingRole = optionalRole.get();
            existingRole.setName(updatedRole.getName());
            return roleRepository.save(existingRole);
        }
        return null; // Role not found
    }

    public void deleteRole(Long roleId) {
        roleRepository.deleteById(roleId);
    }
}
