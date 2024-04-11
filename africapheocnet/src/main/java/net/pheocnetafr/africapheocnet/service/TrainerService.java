package net.pheocnetafr.africapheocnet.service;

import net.pheocnetafr.africapheocnet.entity.Deployment;
import net.pheocnetafr.africapheocnet.entity.Member;
import net.pheocnetafr.africapheocnet.entity.Trainer;
import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.repository.DeploymentRepository;
import net.pheocnetafr.africapheocnet.repository.MemberRepository;
import net.pheocnetafr.africapheocnet.repository.TrainerRepository;
import net.pheocnetafr.africapheocnet.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TrainerService {

    @Autowired
    private TrainerRepository trainerRepository;
    
    @Autowired
    private MemberRepository memberRepository;


    @Autowired
    private BCryptPasswordEncoder passwordEncoder; 

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DeploymentRepository deploymentRepository;

    public List<Trainer> getAllTrainers() {
        return trainerRepository.findAll();
    }

    public Trainer getTrainerById(Long id) {
        Optional<Trainer> optionalTrainer = trainerRepository.findById(id);
        return optionalTrainer.orElse(null);
    }

    public String addNewTrainer(Trainer trainer) {
        // Check if the trainer's email already exists in the trainer table
        Trainer existingTrainer = trainerRepository.findByEmail(trainer.getEmail());
        if (existingTrainer != null) {
            return "Trainer is already registered. Check entry!";
        }

        // Check if the trainer's email exists in the member table
        Member existingMember = memberRepository.findByEmail(trainer.getEmail());
        if (existingMember != null) {
        	 trainer.setNotice("Not filled Yet");
        	 trainer.setStatus("Not filled Yet");
        	 trainer.setLastUpdate(LocalDate.now().toString());
            trainerRepository.save(trainer);
            return "Trainer added successfully!";
        }

        // The trainer is not a member, so register them as a member first
        Member newMember = new Member();
        newMember.setEmail(trainer.getEmail());
        newMember.setBio("Not yet filled");
        newMember.setEnrollment(LocalDate.now());
        newMember.setFirstName(trainer.getFirstName());
        newMember.setLastName(trainer.getLastName());
        newMember.setGender(trainer.getGender());
        newMember.setNationality(trainer.getCountry());
        newMember.setProfession(trainer.getProfession());
        newMember.setOrganization(trainer.getOrganization());
        newMember.setPosition(trainer.getPosition());
        newMember.setExpertise(trainer.getExpertise());
        newMember.setLanguage(trainer.getLanguage());

        // Generate a temporary password
        String temporaryPassword = generateTemporaryPassword();

        // Save the new member
        memberRepository.save(newMember);

        // Send a password reset email to the trainer
        emailService.sendPasswordResetEmail(newMember.getFirstName(),newMember.getEmail(), temporaryPassword);

        // Create a new user in tbl_user for login purposes
        User user = new User();
        user.setEmail(trainer.getEmail());

        // Generate a random password (encrypted with bcrypt)
        String randomPassword = generateRandomPassword();
        String hashedUserPassword = passwordEncoder.encode(randomPassword);
        user.setPassword(hashedUserPassword);
        user.setRole("USER");

        user.setEnabled(true); 
        userRepository.save(user);

        // Save the trainer
        trainerRepository.save(trainer);

        return "Trainer added successfully!";
    }
   
    private String generateRandomPassword() {
        // Generate a random password, such as using UUID
        return UUID.randomUUID().toString();
    }


    private String generateTemporaryPassword() {
        // Generate a unique temporary password using UUID
        return UUID.randomUUID().toString();
    }
    
    public String generateResetToken() {
        // Generate a unique reset token here, such as using UUID
        String resetToken = UUID.randomUUID().toString();
        return resetToken;
    }

    public Trainer updateTrainerByAdmin(Long id, Trainer updatedTrainer) {
        Optional<Trainer> optionalTrainer = trainerRepository.findById(id);
        if (optionalTrainer.isPresent()) {
            Trainer existingTrainer = optionalTrainer.get();
            // Update fields here
            existingTrainer.setFirstName(updatedTrainer.getFirstName());
            existingTrainer.setLastName(updatedTrainer.getLastName());
            existingTrainer.setGender(updatedTrainer.getGender());
            existingTrainer.setCountry(updatedTrainer.getCountry());
            existingTrainer.setCohort(updatedTrainer.getCohort());
            existingTrainer.setLanguage(updatedTrainer.getLanguage());
            existingTrainer.setOrganization(updatedTrainer.getOrganization());
            existingTrainer.setPosition(updatedTrainer.getPosition());
            existingTrainer.setTelephone(updatedTrainer.getTelephone());
            existingTrainer.setEmail(updatedTrainer.getEmail());
            existingTrainer.setNotice(updatedTrainer.getNotice());

            // Update CV, if provided
            byte[] updatedCv = updatedTrainer.getCv();
            if (updatedCv != null && updatedCv.length > 0) {
                existingTrainer.setCv(updatedCv);
            }

            // Update availability, if provided
            String updatedAvailability = updatedTrainer.getStatus();
            if (updatedAvailability != null) {
                existingTrainer.setStatus(updatedAvailability);
            }

            // Update "last update" timestamp
            existingTrainer.setLastUpdate(LocalDateTime.now().toString());

            return trainerRepository.save(existingTrainer);
        }
        return null; // Trainer not found
    }

    public Trainer updateTrainerByTrainer(Long id, Trainer updatedTrainer) {
        Optional<Trainer> optionalTrainer = trainerRepository.findById(id);
        if (optionalTrainer.isPresent()) {
            Trainer existingTrainer = optionalTrainer.get();
            
            // Update CV, if provided by trainer
            byte[] updatedCv = updatedTrainer.getCv();
            if (updatedCv != null && updatedCv.length > 0) {
                existingTrainer.setCv(updatedCv);
                // Update "last update" timestamp
                existingTrainer.setLastUpdate(LocalDateTime.now().toString());
            }

            // Update notice, if provided by trainer
            String updatedNotice = updatedTrainer.getNotice();
            if (updatedNotice != null) {
                existingTrainer.setNotice(updatedNotice);
                // Update "last update" timestamp
                existingTrainer.setLastUpdate(LocalDateTime.now().toString());
            }

            // Update availability, if provided by trainer
            String updatedAvailability = updatedTrainer.getStatus();
            if (updatedAvailability != null) {
                existingTrainer.setStatus(updatedAvailability);
                // Update "last update" timestamp
                existingTrainer.setLastUpdate(LocalDateTime.now().toString());
            }

            return trainerRepository.save(existingTrainer);
        }
        return null; // Trainer not found
    }

    public void deleteTrainer(Long id) {
        // First, check if there are any associated records in the deployment table
        List<Deployment> deployments = deploymentRepository.findByTrainerId(id);
        
        if (!deployments.isEmpty()) {
            
            deploymentRepository.deleteAll(deployments);
        }

       
        trainerRepository.deleteById(id);
    }
    
   
    public List<Trainer> getAllTrainersWithDeploymentStatus() {
        List<Trainer> trainers = trainerRepository.findAll();

        for (Trainer trainer : trainers) {
            boolean deployed = deploymentRepository.existsByTrainerId(trainer.getId());
            trainer.setDeployed(deployed);

            System.out.println("Trainer: " + trainer.getFirstName() + ", Deployed: " + trainer.isDeployed());
        }

        return trainers;
    }
    
    public List<Trainer> getFilteredTrainersByExpertise(String expertise) {
        return trainerRepository.findByExpertise(expertise);
    }
    
    public Trainer getTrainerByEmail(String email) {
        // Implement logic to retrieve trainer by email from the repository
        return trainerRepository.findByEmail(email);
    }
    
    public void saveTrainer(Trainer trainer) {
        trainerRepository.save(trainer);
    }
    
    

}
