package net.pheocnetafr.africapheocnet.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.pheocnetafr.africapheocnet.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

	Optional<Notification> findByEmail(String email);

    List<Notification> findByIsEnable(boolean isEnable);
    
 
    
}
