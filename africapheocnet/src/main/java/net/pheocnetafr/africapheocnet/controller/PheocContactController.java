package net.pheocnetafr.africapheocnet.controller;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import net.pheocnetafr.africapheocnet.CountryList;
import net.pheocnetafr.africapheocnet.entity.Member;
import net.pheocnetafr.africapheocnet.entity.PheocContact;
import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.forum.ForumTopicRepository;
import net.pheocnetafr.africapheocnet.repository.MemberRepository;
import net.pheocnetafr.africapheocnet.repository.PheocContactRepository;
import net.pheocnetafr.africapheocnet.repository.TrainerRepository;
import net.pheocnetafr.africapheocnet.repository.UserRepository;
import java.util.Optional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.Base64;
import java.util.List;

import org.springframework.security.core.Authentication;
@Controller
@RequestMapping("/contacts")
public class PheocContactController {
	
	 @Autowired
	    private TrainerRepository trainerRepository; 
	 
	 @Autowired
	    private UserRepository userRepository;
	 
	 @Autowired
	    private ForumTopicRepository forumTopicRepository;

    @Autowired
    private PheocContactRepository pheocContactRepository;
    
    @Autowired
    private MemberRepository memberRepository;

    
    @ModelAttribute("countries")
    public List<String> getAllCountries() {
        return CountryList.getAllAfricanCountries();
    }
    @GetMapping("/list")
    public String listPheocContacts(Model model, Authentication authentication, Principal principal) {
    	addUserInfoToModel(model, principal);
        List<PheocContact> contacts = pheocContactRepository.findAll();
        model.addAttribute("contacts", contacts);
        model.addAttribute("pageTitle", "Contacts List | Africa PHEOC-Net");

        long totalUsers = userRepository.count();
        long totalTrainedTrainers = trainerRepository.count();
        long totalDiscussions = forumTopicRepository.count();

        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalTrainedTrainers", totalTrainedTrainers);
        model.addAttribute("totalDiscussions", totalDiscussions);
        model.addAttribute("isAdmin", isAdmin);

        return "contactlist";
    }
    
    
    @GetMapping("/list-contact")
    public String listPheocFocalPoints(Model model, Authentication authentication, Principal principal) {
    	addUserInfoToModel(model, principal);
    	List<PheocContact> contacts = pheocContactRepository.findAll();
        model.addAttribute("contacts", contacts);
        model.addAttribute("pageTitle", "Contacts List | Africa PHEOC-Net");

        long totalUsers = userRepository.count();
        long totalTrainedTrainers = trainerRepository.count();
        long totalDiscussions = forumTopicRepository.count();

        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalTrainedTrainers", totalTrainedTrainers);
        model.addAttribute("totalDiscussions", totalDiscussions);
        model.addAttribute("isAdmin", isAdmin);

        return "contact-list";
    }


    
 // Controller method to display the form for adding a new PHEOC contact
    @GetMapping("/add")
    public String showAddContactForm(Model model, Principal principal) {
    	addUserInfoToModel(model, principal);
        model.addAttribute("pheocContact", new PheocContact());
        return "contacts/add";
    }


 // Add a new PHEOC contact (POST request)
    @PostMapping("/add")
    public ResponseEntity<String> addPheocContact(@ModelAttribute PheocContact pheocContact) {
        try {
            pheocContactRepository.save(pheocContact);
            return ResponseEntity.ok("Contact added successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add contact");
        }
    }


    // Update an existing PHEOC contact (GET request)

    @GetMapping("/edit/{id}")
    public String viewContactDetails(@PathVariable Long id, Model model, Principal principal) {
    	addUserInfoToModel(model, principal);
        Optional<PheocContact> optionalContact = pheocContactRepository.findById(id);

        if (optionalContact.isPresent()) {
            PheocContact contact = optionalContact.get();
            model.addAttribute("contact", contact);
            model.addAttribute("countries", getAllCountries());
            model.addAttribute("pageTitle", "Edit Contact | Africa PHEOC-Net");
            return "edit-contact";
        } else {
            return "contact-not-found";
        }
    }

    // Update an existing PHEOC contact (POST request)
    @PostMapping("/update/{id}")
    public String editPheocContact(@PathVariable Long id, @ModelAttribute PheocContact updatedContact) {
        PheocContact existingContact = pheocContactRepository.findById(id).orElse(null);
        if (existingContact != null) {
            // Update the fields of the existing contact
            existingContact.setCountry(updatedContact.getCountry());
            existingContact.setFirstName(updatedContact.getFirstName());
            existingContact.setLastName(updatedContact.getLastName());
            existingContact.setDepartment(updatedContact.getDepartment());
            existingContact.setPosition(updatedContact.getPosition());
            existingContact.setMobile(updatedContact.getMobile());
            existingContact.setEmail(updatedContact.getEmail());

            // Save the updated contact
            pheocContactRepository.save(existingContact);
        }
        return "redirect:/contacts/list";
    }

    // Delete a PHEOC contact
    @PostMapping("/delete/{id}")
    public ResponseEntity<String> deletePheocContact(@PathVariable Long id) {
        try {
            pheocContactRepository.deleteById(id);
            return ResponseEntity.ok("Contact deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete contact");
        }
    }

    
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportContactsToExcel() {
        List<PheocContact> contacts = pheocContactRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Contacts");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Country", "First Name", "Last Name", "Department", "Position", "Mobile", "Email"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Populate data rows
            int rowNum = 1;
            for (PheocContact contact : contacts) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(contact.getCountry());
                row.createCell(1).setCellValue(contact.getFirstName());
                row.createCell(2).setCellValue(contact.getLastName());
                row.createCell(3).setCellValue(contact.getDepartment());
                row.createCell(4).setCellValue(contact.getPosition());
                row.createCell(5).setCellValue(contact.getMobile());
                row.createCell(6).setCellValue(contact.getEmail());
            }

            // Convert workbook to byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            workbook.write(byteArrayOutputStream);

            // Create ResponseEntity with byte array and appropriate headers
            HttpHeaders header = new HttpHeaders();
            header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contacts_export.xlsx");

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
