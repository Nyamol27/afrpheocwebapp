package net.pheocnetafr.africapheocnet.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.pheocnetafr.africapheocnet.entity.Deployment;
import net.pheocnetafr.africapheocnet.entity.Trainer;

public interface DeploymentRepository extends JpaRepository<Deployment, Long> {
    List<Deployment> findByTrainer(Trainer trainer);
    List<Deployment> findByTrainerId(Long trainerId);
    boolean existsByTrainerId(Long trainerId);
    List<Deployment> findByTrainerIdAndStatus(Long trainerId, String status);
    Optional<Deployment> findById(Long id);

}

