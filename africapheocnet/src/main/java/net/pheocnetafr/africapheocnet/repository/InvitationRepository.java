package net.pheocnetafr.africapheocnet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import net.pheocnetafr.africapheocnet.entity.Invitation;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
	
	boolean existsByReceiverEmail(String email);
}
