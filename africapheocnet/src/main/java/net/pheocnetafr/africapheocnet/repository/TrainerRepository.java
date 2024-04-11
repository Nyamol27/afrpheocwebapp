package net.pheocnetafr.africapheocnet.repository;

import net.pheocnetafr.africapheocnet.entity.Trainer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {
	List<Trainer> findByExpertise(String expertise);
	 Trainer findByEmail(String email);
	 
	 
	// Method to count trained personnel by country
	    @Query("SELECT tr.country, COUNT(tr) FROM TraineeRegistry tr GROUP BY tr.country")
	    List<Object[]> countTrainedPersonnelByCountry();
}
