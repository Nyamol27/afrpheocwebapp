package net.pheocnetafr.africapheocnet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.pheocnetafr.africapheocnet.entity.TraineeRegistry;
import net.pheocnetafr.africapheocnet.repository.TraineeRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TraineeService {

    @Autowired
    private TraineeRepository traineeRepository;

    // Add a trainee
    public void addTrainee(TraineeRegistry trainee) {
        traineeRepository.save(trainee);
    }

    // Delete a trainee by id
    public void deleteTrainee(Long id) {
        traineeRepository.deleteById(id);
    }

    // Get all trainees
    public List<TraineeRegistry> getAllTrainees() {
        return traineeRepository.findAll();
    }

 // Update trainee details
    public void updateTrainee(Long id, TraineeRegistry trainee) {
        Optional<TraineeRegistry> existingTraineeOptional = traineeRepository.findById(id);
        if (existingTraineeOptional.isPresent()) {
            TraineeRegistry existingTrainee = existingTraineeOptional.get();
            existingTrainee.setFirstName(trainee.getFirstName());
            existingTrainee.setLastName(trainee.getLastName());
            existingTrainee.setEmail(trainee.getEmail());
            existingTrainee.setPhoneNumber(trainee.getPhoneNumber());
            existingTrainee.setOrganization(trainee.getOrganization());
            existingTrainee.setPosition(trainee.getPosition());
            existingTrainee.setModuleAttended(trainee.getModuleAttended());
            existingTrainee.setTrainingDate(trainee.getTrainingDate());
            existingTrainee.setCountry(trainee.getCountry());
            existingTrainee.setRole(trainee.getRole());
            
            // Save the updated trainee entity
            traineeRepository.save(existingTrainee);
        }
    }

    
    public TraineeRegistry getTraineeById(Long id) {
        Optional<TraineeRegistry> traineeOptional = traineeRepository.findById(id);
        return traineeOptional.orElse(null);
    }
}
