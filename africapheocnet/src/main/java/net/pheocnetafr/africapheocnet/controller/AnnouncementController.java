package net.pheocnetafr.africapheocnet.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import net.pheocnetafr.africapheocnet.entity.Announcement;
import net.pheocnetafr.africapheocnet.entity.Member;
import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.repository.AnnouncementRepository;
import net.pheocnetafr.africapheocnet.repository.MemberRepository;
import net.pheocnetafr.africapheocnet.repository.UserRepository;
import net.pheocnetafr.africapheocnet.service.AnnouncementService;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import net.pheocnetafr.africapheocnet.util.ClamAVService;
import net.pheocnetafr.africapheocnet.util.VirusScanResult;
import net.pheocnetafr.africapheocnet.util.VirusScanStatus;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.sql.Blob;
@Controller
@RequestMapping("/announcement")
public class AnnouncementController {
	

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    private final AnnouncementService announcementService;

    @Autowired
    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }
   
    @GetMapping("/list")
    public String listAnnouncements(Model model, Principal principal) {
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

        List<Announcement> announcements = announcementService.getAllAnnouncements();

        model.addAttribute("announcements", announcements);
        model.addAttribute("totalAnnouncements", announcements.size());
        model.addAttribute("pageTitle", "Announcements List | Africa PHEOC-Net");

        return "announcement-list";
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
    @GetMapping("/list-announcement")
    public String listAllAnnouncements(Model model,Principal principal) {
    	
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
        // Fetch announcements sorted by published date in descending order
        List<Announcement> announcements = announcementService.getAllAnnouncementsSortedByPublicationDateDesc();

        model.addAttribute("announcements", announcements);
        model.addAttribute("totalAnnouncements", announcements.size()); 
        model.addAttribute("pageTitle", "Announcements List | Africa PHEOC-Net");
        
        return "announcements";
    }


    @GetMapping("/add")
    public String showAddAnnouncementForm(Model model) {
        model.addAttribute("announcement", new Announcement());
        return "announcement/add";
    }

    @PostMapping("/add")
    public ResponseEntity<String> addAnnouncement(
            @RequestParam("announcementTitle") String announcementTitle,
            @RequestParam("announcementContent") String announcementContent,
            @RequestParam(value = "announcementAttachment", required = false) MultipartFile announcementAttachment) {

        try {
            // Check for CSRF token validation here if needed

            if (announcementAttachment != null && !announcementAttachment.isEmpty()) {
                // Process announcement with attachment
                return processAnnouncementWithAttachment(announcementTitle, announcementContent, announcementAttachment);
            } else {
                // Process announcement without attachment
                return processAnnouncementWithoutAttachment(announcementTitle, announcementContent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding announcement");
        }
    }

    private ResponseEntity<String> processAnnouncementWithAttachment(String announcementTitle,
            String announcementContent, MultipartFile announcementAttachment) throws IOException {
        // Validate file type and size here
        if (!isValidAttachment(announcementAttachment)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid attachment");
        }

        // Initialize ClamAV service
        ClamAVService clamAVService = new ClamAVService();

        // Check if ClamAV is reachable
        if (!clamAVService.ping()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to connect to ClamAV service.");
        }

        // Perform virus scan
        VirusScanResult scanResult;
        try {
            scanResult = clamAVService.scan(announcementAttachment.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to scan attachment for viruses");
        }

        // Check scan result
    
        if (scanResult.getStatus() != VirusScanStatus.PASSED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Virus found in attachment: " + scanResult.getSignature());
        }


        String attachmentType = announcementAttachment.getContentType(); // Retrieve MIME type

        // Save the announcement with attachment
        Announcement announcement = new Announcement();
        announcement.setSubject(announcementTitle);
        announcement.setContent(announcementContent);
        announcement.setAttachmentType(attachmentType);
        announcement.setPublicationDate(new Date());
        announcement.setStatus("Broadcasted");

        announcementService.addAnnouncementWithAttachment(announcement, announcementAttachment);

        return ResponseEntity.ok("Announcement added successfully");
    }


    private ResponseEntity<String> processAnnouncementWithoutAttachment(String announcementTitle,
            String announcementContent) {
        // Save the announcement without attachment
        Announcement announcement = new Announcement();
        announcement.setSubject(announcementTitle);
        announcement.setContent(announcementContent);
        announcement.setPublicationDate(new Date());
        announcement.setStatus("Broadcasted");
        
        announcementService.addAnnouncementWithoutAttachment(announcement);

        return ResponseEntity.ok("Announcement added successfully");
    }


    private boolean isValidAttachment(MultipartFile attachment) {
        // Example validation: Check file type and size
        if (attachment == null || attachment.isEmpty()) {
            return false;
        }

        // Example: Allowed file types
        List<String> allowedTypes = Arrays.asList("image/jpeg", "image/png", "application/pdf");

        
        long maxSize = 200L * 1024 * 1024; 

        return allowedTypes.contains(attachment.getContentType()) && attachment.getSize() <= maxSize;
    }
    
    
    @GetMapping("/view/{id}")
    public String showAnnouncement(@PathVariable Long id, Model model,Principal principal) {
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
        Announcement announcement = announcementService.getAnnouncementById(id);
        model.addAttribute("announcement", announcement);
        model.addAttribute("pageTitle", "Announcements  | Africa PHEOC-Net");
        return "announcement-edit";
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<String> updateAnnouncement(@PathVariable Long id,
                                                     @RequestParam("announcementTitle") String announcementTitle,
                                                     @RequestParam("announcementContent") String announcementContent,
                                                     @RequestParam(value = "attachment", required = false) MultipartFile attachment) {
        try {
            // Find the announcement by ID
            Optional<Announcement> optionalAnnouncement = announcementService.findById(id);
            if (optionalAnnouncement.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Announcement not found with ID: " + id);
            }

            // Get the announcement object from Optional
            Announcement announcement = optionalAnnouncement.get();

            // Update announcement title and content
            announcement.setSubject(announcementTitle);
            announcement.setContent(announcementContent);

         // Handle attachment if provided
            if (attachment != null && !attachment.isEmpty()) {
                // Update attachment only if a new file is provided
                byte[] attachmentBytes = attachment.getBytes();
                announcement.setAttachment(attachmentBytes);
                announcement.setAttachmentAvailable(true);
                announcement.setAttachmentType(attachment.getContentType());
            } else {
                // If no new attachment is provided, explicitly set attachment fields to null or empty
                announcement.setAttachment(null);
                announcement.setAttachmentAvailable(false);
                announcement.setAttachmentType(null);
            }


            // Save the updated announcement
            announcementService.save(announcement);

            return ResponseEntity.ok("Announcement updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while updating the announcement.");
        }
    }


    @PostMapping("/delete/{id}")
    public ResponseEntity<String> deleteAnnouncement(@PathVariable Long id) {
        try {
            announcementService.deleteAnnouncement(id);
            return ResponseEntity.ok("Announcement deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while deleting the announcement.");
        }
    }

    
    @GetMapping("/downloadAttachment/{id}")
    @ResponseBody
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long id) {
        Announcement announcement = announcementService.getAnnouncementById(id);

        if (announcement != null && announcement.getAttachment() != null) {
            ByteArrayResource resource = new ByteArrayResource(announcement.getAttachment());

           
            String contentType = "application/octet-stream"; 

            
            if (announcement.getAttachmentType() != null && !announcement.getAttachmentType().isEmpty()) {
                contentType = determineContentType(announcement.getAttachmentType());
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + generateFileName(announcement) + "\"")
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private String determineContentType(String attachmentType) {
       
        Map<String, String> extensionContentTypeMap = new HashMap<>();
        extensionContentTypeMap.put("pdf", "application/pdf");
        extensionContentTypeMap.put("doc", "application/msword");
        extensionContentTypeMap.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        extensionContentTypeMap.put("txt", "text/plain");
        String fileExtension = attachmentType.toLowerCase(); 

       
        String contentType = extensionContentTypeMap.get(fileExtension);

    
        return (contentType != null) ? contentType : "application/octet-stream";
    }


    private String generateFileName(Announcement announcement) {
        String fileName = "attachment_" + announcement.getId(); 

       
        if (announcement.getSubject() != null && !announcement.getSubject().isEmpty()) {
           
            int maxLength = 50;
            String truncatedSubject = announcement.getSubject().substring(0, Math.min(announcement.getSubject().length(), maxLength));

          
            String sanitizedSubject = truncatedSubject.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
            fileName = sanitizedSubject + "_" + announcement.getId();
        }

        return fileName;
    }
  
    @GetMapping("/edit/{id}")
    public String editAnnouncement(@PathVariable Long id, Model model, Principal principal) {
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
        Optional<Announcement> optionalAnnouncement = announcementRepository.findById(id);
        if (optionalAnnouncement.isPresent()) {
            Announcement announcement = optionalAnnouncement.get();
            
            // Set attachmentAvailable based on whether attachment exists
            announcement.setAttachmentAvailable(announcement.getAttachment() != null);
            
            model.addAttribute("announcement", announcement);
            return "edit-announcement";
        } else {
            return "announcement-not-found";
        }
    }


}

