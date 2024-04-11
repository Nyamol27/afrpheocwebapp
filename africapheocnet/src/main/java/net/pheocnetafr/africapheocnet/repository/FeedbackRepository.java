package net.pheocnetafr.africapheocnet.repository;

import net.pheocnetafr.africapheocnet.entity.Feedback;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
	List<Feedback> findByStatus(String status);
}
