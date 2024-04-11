package net.pheocnetafr.africapheocnet.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.pheocnetafr.africapheocnet.entity.Deployment;
import net.pheocnetafr.africapheocnet.entity.Trainer;
import net.pheocnetafr.africapheocnet.repository.DeploymentRepository;

@Service
public class DeploymentService {
    @Autowired
    private DeploymentRepository deploymentRepository;

    public List<Deployment> getAllDeployments() {
        return deploymentRepository.findAll();
    }

    public List<Deployment> getDeploymentsByTrainer(Trainer trainer) {
    	Deployment deployment = new Deployment();

        Long trainerId = deployment.getId(); 
        List<Deployment> existingDeployments = deploymentRepository.findByTrainerIdAndStatus(trainerId, "Ongoing");

        if (!existingDeployments.isEmpty()) {
           
            Deployment existingDeployment = existingDeployments.get(0);
            throw new RuntimeException("Trainer is currently deployed in " +
                    existingDeployment.getCountry() + " as " + existingDeployment.getRole());
        }
        return deploymentRepository.findByTrainer(trainer);
    }

    public Deployment createDeployment(Deployment deployment) {
        return deploymentRepository.save(deployment);
    }

    public Deployment updateDeployment(Long id, Deployment updatedDeployment) {
        Optional<Deployment> optionalDeployment = deploymentRepository.findById(id);
        if (optionalDeployment.isPresent()) {
            Deployment existingDeployment = optionalDeployment.get();
            // Update fields with values from updatedDeployment
            existingDeployment.setStatus(updatedDeployment.getStatus());
            existingDeployment.setType(updatedDeployment.getType());
            existingDeployment.setExpertise(updatedDeployment.getExpertise());
            existingDeployment.setRole(updatedDeployment.getRole());
            existingDeployment.setCountry(updatedDeployment.getCountry());
            existingDeployment.setStartDate(updatedDeployment.getStartDate());
            existingDeployment.setEndDate(updatedDeployment.getEndDate());
            existingDeployment.setDeployingOrganism(updatedDeployment.getDeployingOrganism());
            existingDeployment.setTrainerEmail(updatedDeployment.getTrainerEmail());
            // Update other fields as needed
            return deploymentRepository.save(existingDeployment);
        }
        return null; // Deployment not found
    }


    public void deleteDeployment(Long id) {
        deploymentRepository.deleteById(id);
    }
    
    public Deployment getDeploymentById(Long id) {
        return deploymentRepository.findById(id).orElse(null);
    }
}

