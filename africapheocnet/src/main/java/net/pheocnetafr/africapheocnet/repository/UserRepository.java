package net.pheocnetafr.africapheocnet.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.pheocnetafr.africapheocnet.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email); 
    
    User findByEmailAndResetToken(String email, String resetToken);
    
    List<User> findByRole(String role);
	
    User findByUsername(String username);
    
    List<User> findByRoleIn(List<String> roles);
    
    Optional<User> findById(Long userId);
    
    User findByResetToken(String resetToken);
}
