package net.pheocnetafr.africapheocnet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.pheocnetafr.africapheocnet.entity.TraineeRegistry;

@Repository
public interface TraineeRepository extends JpaRepository<TraineeRegistry, Long> {
	
   
}
