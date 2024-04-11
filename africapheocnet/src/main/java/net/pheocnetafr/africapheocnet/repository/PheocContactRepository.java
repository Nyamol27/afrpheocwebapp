package net.pheocnetafr.africapheocnet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import net.pheocnetafr.africapheocnet.entity.PheocContact;

public interface PheocContactRepository extends JpaRepository<PheocContact, Long> {
    
}
