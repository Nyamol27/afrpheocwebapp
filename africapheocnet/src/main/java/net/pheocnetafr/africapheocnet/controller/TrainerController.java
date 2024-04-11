package net.pheocnetafr.africapheocnet.controller;
import org.apache.commons.io.FilenameUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.attribute.UserPrincipal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.util.FileSystemUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import net.pheocnetafr.africapheocnet.entity.Deployment;
import net.pheocnetafr.africapheocnet.entity.Member;
import net.pheocnetafr.africapheocnet.entity.Trainer;
import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.repository.DeploymentRepository;
import net.pheocnetafr.africapheocnet.repository.MemberRepository;
import net.pheocnetafr.africapheocnet.repository.TrainerRepository;
import net.pheocnetafr.africapheocnet.repository.UserRepository;
import net.pheocnetafr.africapheocnet.service.TrainerService;
import net.pheocnetafr.africapheocnet.util.AutoFilledData;

import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import io.micrometer.common.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pheocnetafr.africapheocnet.util.ClamAVService;
import net.pheocnetafr.africapheocnet.util.VirusScanResult;
import net.pheocnetafr.africapheocnet.util.VirusScanStatus;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.sql.Blob;
@Controller
@RequestMapping("/roster")
public class TrainerController {

	private static final Logger log = LoggerFactory.getLogger(TrainerController.class);
	@Autowired
	private SpringTemplateEngine templateEngine;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private DeploymentRepository deploymentRepository;

    @Autowired
    private TrainerService trainerService;
    
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/trainerCount")
    public String getTrainerCount(Model model) {
        long totalTrainers = trainerRepository.count();
        model.addAttribute("totalTrainers", totalTrainers);
        return "trainerCount";
    }

    @GetMapping("/list")
    public String listTrainers(Model model, Principal principal) {
        try {
        	addUserInfoToModel(model, principal);
            List<Trainer> trainers = trainerService.getAllTrainersWithDeploymentStatus();

            // Iterate through trainers and set deployment status
            for (Trainer trainer : trainers) {
                boolean deployed = deploymentRepository.existsByTrainerId(trainer.getId());
                trainer.setDeployed(deployed);
            }

            model.addAttribute("trainers", trainers);
            model.addAttribute("pageTitle", "PHEOC Roster | Africa PHEOC-Net");

           

        } catch (Exception e) {
            model.addAttribute("error", "Error retrieving trainer list");
        }
        return "trainer-list";
    }




    @GetMapping("/view/{id}")
    public String viewTrainerDetails(@PathVariable Long id, Model model,Principal principal) {
    	addUserInfoToModel(model, principal);
        Optional<Trainer> optionalTrainer = trainerRepository.findById(id);
        if (optionalTrainer.isPresent()) {
            Trainer trainer = optionalTrainer.get();
            model.addAttribute("trainer", trainer);
            model.addAttribute("pageTitle", "PHEOC Roster | Africa PHEOC-Net");
            List<Deployment> deployments = deploymentRepository.findByTrainerId(id);
            model.addAttribute("deployments", deployments);

            return "view-trainer";
        } else {
            return "trainer-not-found";
        }
    }

    @GetMapping("/edit/{id}")
    public String editTrainerDetails(@PathVariable Long id, Model model, Principal principal) {
        return trainerRepository.findById(id)
                .map(trainer -> {
                	addUserInfoToModel(model, principal);
                    model.addAttribute("trainer", trainer);
                    model.addAttribute("pageTitle", "PHEOC Roster | Africa PHEOC-Net");
                    return "trainer-edit";
                })
                .orElse("trainer-not-found");
    }


    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    public ResponseEntity<String> updateTrainerDetails(@PathVariable Long id, @ModelAttribute Trainer updatedTrainer) {
        try {
            Optional<Trainer> optionalTrainer = trainerRepository.findById(id);
            if (optionalTrainer.isPresent()) {
                Trainer existingTrainer = optionalTrainer.get();
                // Update trainer details
                existingTrainer.setFirstName(updatedTrainer.getFirstName());
                existingTrainer.setLastName(updatedTrainer.getLastName());
                existingTrainer.setCohort(updatedTrainer.getCohort());
                existingTrainer.setCountry(updatedTrainer.getCountry());
                existingTrainer.setGender(updatedTrainer.getGender());
                existingTrainer.setOrganization(updatedTrainer.getOrganization());
                existingTrainer.setPosition(updatedTrainer.getPosition());
                existingTrainer.setEmail(updatedTrainer.getEmail());
                existingTrainer.setTelephone(updatedTrainer.getTelephone());
                existingTrainer.setLanguage(updatedTrainer.getLanguage());

                // Save the updated trainer
                trainerRepository.save(existingTrainer);
                return ResponseEntity.ok("Trainer details updated successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trainer not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update trainer details");
        }
    }
    
    
    @PostMapping("/addTrainer")
    public ResponseEntity<String> addTrainer(@ModelAttribute Trainer trainer) {
        try {
            String message = trainerService.addNewTrainer(trainer);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding trainer: " + e.getMessage());
        }
    }
    
    @PostMapping("/deleteTrainer")
    public ResponseEntity<String> deleteTrainer(@RequestParam Long trainerId) {
        try {
            // Call the service method to delete the trainer
            trainerService.deleteTrainer(trainerId);

            // Return a success message
            return ResponseEntity.ok("Trainer deleted successfully!");
        } catch (Exception e) {
            // Return an error message
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting trainer: " + e.getMessage());
        }
    }
    
    @GetMapping("/filterByExpertise")
    @ResponseBody
    public ResponseEntity<List<Trainer>> filterByExpertise(@RequestParam(required = false) String expertise) {
        try {
            List<Trainer> filteredTrainers;

            if (StringUtils.isEmpty(expertise)) {
                // If expertise is empty, retrieve all trainers
                filteredTrainers = trainerService.getAllTrainers();
            } else {
                // Otherwise, filter trainers by expertise
                filteredTrainers = trainerService.getFilteredTrainersByExpertise(expertise);
            }

            return ResponseEntity.ok(filteredTrainers);
        } catch (Exception e) {
            // Log the exception for debugging purposes
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/exportTrainersToExcel")
    public ResponseEntity<byte[]> exportTrainersToExcel() {
        List<Trainer> trainers = trainerRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Trainers");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"First Name", "Last Name", "Cohort", "Country", "Language", "Expertise", "Status", "Deployed"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Populate data rows
            int rowNum = 1;
            for (Trainer trainer : trainers) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(trainer.getFirstName());
                row.createCell(1).setCellValue(trainer.getLastName());
                row.createCell(2).setCellValue(trainer.getCohort());
                row.createCell(3).setCellValue(trainer.getCountry());
                row.createCell(4).setCellValue(trainer.getLanguage());
                row.createCell(5).setCellValue(trainer.getExpertise());
                row.createCell(6).setCellValue(trainer.getStatus());
                row.createCell(7).setCellValue(trainer.isDeployed() ? "Yes" : "No");
            }

            // Convert workbook to byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            workbook.write(byteArrayOutputStream);

            // Create ResponseEntity with byte array and appropriate headers
            HttpHeaders header = new HttpHeaders();
            header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=trainers_export.xlsx");

            return ResponseEntity
                    .ok()
                    .headers(header)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(byteArrayOutputStream.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/downloadCV/{trainerId}")
    public ResponseEntity<byte[]> downloadCV(@PathVariable Long trainerId) {
        try {
            // Retrieve the trainer from the service
            Trainer trainer = trainerService.getTrainerById(trainerId);

            // Check if the trainer or CV data is null
            if (trainer == null || trainer.getCv() == null) {
                // Log a warning or handle the case where the trainer or CV is not found
                log.warn("Trainer or CV not found for trainerId: {}", trainerId);
                return ResponseEntity.notFound().build();
            }

            // Get CV data and type
            byte[] cvBytes = trainer.getCv();
            String cvType = trainer.getCvType();

            // Perform a null check on cvType
            if (cvType == null) {
                log.warn("CV type is null for trainerId: {}", trainerId);
                return ResponseEntity.badRequest().build(); 
            }

            // Create response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(getMediaType(cvType));

            // Update filename with a null check
            String filename = "Trainer_CV." + cvType;
            headers.setContentDisposition(ContentDisposition.builder("inline").filename(filename).build());

            // Return the response entity
            return new ResponseEntity<>(cvBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            // Log the exception for troubleshooting
            log.error("Error while downloading CV for trainerId: {}", trainerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



    private MediaType getMediaType(String fileType) {
        if ("pdf".equalsIgnoreCase(fileType)) {
            return MediaType.APPLICATION_PDF;
        } else if ("docx".equalsIgnoreCase(fileType)) {
            return MediaType.APPLICATION_OCTET_STREAM; 
        }
       
        return MediaType.APPLICATION_OCTET_STREAM; 
    }

    @GetMapping("/settings")
    public String showTrainerSettingsForm(Model model, Principal principal) {
        try {
        	
        	addUserInfoToModel(model, principal);
            // Retrieve the authenticated user's details
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Retrieve user data based on the email
            Trainer trainer = trainerService.getTrainerByEmail(userDetails.getUsername());

            if (trainer == null) {
                // Handle case where user is not found, maybe redirect to an error page
                return "error";
            }

            // Add the trainer object to the model
            model.addAttribute("trainer", trainer);

            // Populate noticeOptions and trainerStatusList from AutoFilledData
            model.addAttribute("noticeOptions", AutoFilledData.getTrainerNoticeOptions());
            model.addAttribute("trainerStatusList", AutoFilledData.getTrainerStatusList());
            model.addAttribute("pageTitle", "Trainer Profile & Availability Management | Africa PHEOC-Net");

            return "trainer-settings-form";
        } catch (Exception e) {
            // Handle any exceptions that might occur during data retrieval
            e.printStackTrace(); // Log the exception for debugging
            return "error"; // Redirect to an error page
        }
    }



    @PostMapping("/updateStatus")
    public ResponseEntity<String> updateStatus(@RequestParam("status") String status,
                                               @RequestParam("notice") String notice,
                                               Principal principal) {
        // Retrieve the username of the authenticated user
        String username = principal.getName();

        // Retrieve the trainer details using the username
        Trainer trainer = trainerService.getTrainerByEmail(username);

        if (trainer != null) {
            try {
                // Update the trainer's status and notice period
                trainer.setStatus(status);
                trainer.setNotice(notice);
                trainer.setLastUpdate(LocalDate.now().toString());

                // Save the updated trainer details
                trainerRepository.save(trainer);

                return ResponseEntity.ok("Status and notice period updated successfully.");
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update status and notice period.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trainer not found.");
        }
    }

    @GetMapping("/downloadCV")
    public ResponseEntity<?> downloadCV(Principal principal) {
        try {
            // Retrieve the authenticated username from the principal
            String username = principal.getName();

            // Retrieve the trainer details using the username
            Trainer trainer = trainerService.getTrainerByEmail(username);

            // Check if the trainer or CV data is null
            if (trainer == null || trainer.getCv() == null) {
                // Handle the case where the trainer or CV is not found
                String errorMessage = "CV not found for username: " + username;
                log.warn(errorMessage);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
            }

            // Get CV data and type
            byte[] cvBytes = trainer.getCv();
            String cvType = trainer.getCvType();

            // Perform a null check on cvType
            if (cvType == null) {
                String errorMessage = "CV type is null for username: " + username;
                log.warn(errorMessage);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
            }

            // Create response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(getMediaType(cvType));

            // Update filename with a null check
            String filename = "Trainer_CV." + cvType;
            headers.setContentDisposition(ContentDisposition.builder("inline").filename(filename).build());

            // Return the response entity
            return ResponseEntity.ok().headers(headers).body(cvBytes);
        } catch (Exception e) {
            // Log the exception for troubleshooting
            String errorMessage = "Error while downloading CV for username: " + principal.getName();
            log.error(errorMessage, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }

    @PostMapping("/uploadResume")
    public ResponseEntity<String> uploadResume(@RequestParam("resumeFile") MultipartFile resumeFile, Principal principal) {
        if (resumeFile.isEmpty()) {
            return ResponseEntity.ok("Resume file is empty.");
        }

        // Check file type
        String fileType = resumeFile.getContentType();
        if (!"application/pdf".equals(fileType) && !("application/msword".equals(fileType) || "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(fileType))) {
            return ResponseEntity.ok("Only PDF and Word documents are allowed.");
        }

        // Check file size
        if (resumeFile.getSize() > 15 * 1024 * 1024) { // 15MB
            return ResponseEntity.ok("File size exceeds the limit (15MB).");
        }

        // Scan the file for viruses
        try {
            // Initialize ClamAV service
            ClamAVService clamAVService = new ClamAVService();

            // Check if ClamAV is reachable
            if (!clamAVService.ping()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to connect to ClamAV service.");
            }

            // Perform virus scan
            VirusScanResult scanResult = clamAVService.scan(resumeFile.getInputStream());

            // Check scan result
            if (scanResult.getStatus() == VirusScanStatus.PASSED) {
                // Proceed with uploading the file
                String username = principal.getName();
                Trainer trainer = trainerService.getTrainerByEmail(username);

                if (trainer != null) {
                    // Save the file to the database
                    try {
                        trainer.setCv(resumeFile.getBytes());
                        String fileExtension = FilenameUtils.getExtension(resumeFile.getOriginalFilename());
                        trainer.setCvType(fileExtension != null ? fileExtension : "");
                        trainer.setLastUpdate(LocalDate.now().toString());
                        trainerRepository.save(trainer);
                        return ResponseEntity.ok("Resume uploaded successfully.");
                    } catch (IOException e) {
                        e.printStackTrace();
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload resume.");
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trainer not found.");
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Virus found in resume: " + scanResult.getSignature());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to scan resume for viruses.");
        }
    }


    
    private void addUserInfoToModel(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            User user = userRepository.findByEmail(username);
            if (user != null) {
                String firstName = user.getFirstName();
                String lastName = user.getLastName();
                String initials = firstName.substring(0, 1) + lastName.substring(0, 1);
                model.addAttribute("firstName", firstName);
                model.addAttribute("lastName", lastName);
                model.addAttribute("initials", initials);
            }
            // Fetch member information including the photo
            Member member = memberRepository.findByEmail(username);
            if (member != null) {
                convertPhotoToBase64(member);
                model.addAttribute("member", member);
            }
        }
    }

    private void convertPhotoToBase64(Member member) {
        if (member != null && member.getPhoto() != null) {
            // Convert binary photo data to Base64-encoded string
            byte[] photoBytes = member.getPhoto();
            String base64Photo = Base64.getEncoder().encodeToString(photoBytes);
            // Set the Base64-encoded photo string to the member object
            member.setBase64Photo(base64Photo);
        }
    }
}
