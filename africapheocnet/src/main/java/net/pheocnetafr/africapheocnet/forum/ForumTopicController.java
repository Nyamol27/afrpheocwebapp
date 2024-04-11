package net.pheocnetafr.africapheocnet.forum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;

import jakarta.servlet.http.HttpSession;
import net.pheocnetafr.africapheocnet.entity.Member;
import net.pheocnetafr.africapheocnet.entity.Notification;
import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.repository.MemberRepository;
import net.pheocnetafr.africapheocnet.repository.NotificationRepository;
import net.pheocnetafr.africapheocnet.repository.UserRepository;
import net.pheocnetafr.africapheocnet.service.EmailService;

import java.io.IOException;
import java.security.Principal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.sql.rowset.serial.SerialBlob;

import org.thymeleaf.TemplateEngine;

import java.sql.Timestamp;
import net.pheocnetafr.africapheocnet.util.ClamAVService;
import net.pheocnetafr.africapheocnet.util.VirusScanResult;
import net.pheocnetafr.africapheocnet.util.VirusScanStatus;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.sql.Blob;
@Controller
@RequestMapping("/api/topic")
public class ForumTopicController {
	
	  @Autowired
	  private ForumTopicRepository topicRepository;
	
	 @Autowired
	 private TemplateEngine templateEngine; 
	 
	 @Autowired
	    private MemberRepository memberRepository;
	
	private final EmailService emailService;
	private final NotificationRepository notificationRepository;
	private final ForumTopicService forumTopicService;
	private final ForumCategoryService forumCategoryService;
	private final ForumCommentService forumCommentService;
	private final UserRepository userRepository;
	private final ForumCommentRepository forumCommentRepository;

	@Autowired
	public ForumTopicController(ForumTopicService forumTopicService,
	                            ForumCategoryService forumCategoryService,
	                            ForumCommentService forumCommentService,
	                            UserRepository userRepository,
	                            ForumCommentRepository forumCommentRepository,
	                            EmailService emailService,
	                            NotificationRepository notificationRepository) {
	    this.forumTopicService = forumTopicService;
	    this.forumCategoryService = forumCategoryService;
	    this.forumCommentService = forumCommentService;
	    this.userRepository = userRepository;
	    this.forumCommentRepository = forumCommentRepository;
	    this.emailService = emailService;
	    this.notificationRepository = notificationRepository;
	}

    @GetMapping("/{id}")
    public ResponseEntity<ForumTopic> getTopicById(@PathVariable Long id) {
        Optional<ForumTopic> topic = forumTopicService.getTopicById(id);
        return topic.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    public ResponseEntity<String> createTopic(@RequestParam("title") String title,
                                              @RequestParam("content") String content,
                                              @RequestParam(value = "attachment", required = false) MultipartFile attachment,
                                              @RequestParam("categoryName") String categoryName,
                                              HttpSession session) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        Optional<Long> categoryIdOptional = forumCategoryService.findCategoryIdByName(categoryName);
        if (!categoryIdOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found");
        }

        Long categoryId = categoryIdOptional.get();
        ForumTopic topic = new ForumTopic();
        topic.setTitle(title);
        topic.setContent(content);
        topic.setUser(currentUser);
        topic.setCategory(new ForumCategory(categoryId));
        topic.setStatus("Published");

        if (attachment != null && !attachment.isEmpty()) {
            try {
                // Virus scanning logic before proceeding with the attachment
                ClamAVService clamAVService = new ClamAVService();
                VirusScanResult scanResult = clamAVService.scan(attachment.getInputStream());

                if (scanResult.getStatus() != VirusScanStatus.PASSED) {
                    // Virus found, reject the attachment
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Attachment contains a virus");
                }

                Blob attachmentBlob = new SerialBlob(attachment.getBytes());
                topic.setAttachment(attachmentBlob);

                // Set attachment name
                String attachmentName = attachment.getOriginalFilename(); 
                topic.setAttachmentName(attachmentName);
            } catch (IOException | SQLException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process attachment");
            }
        }

        forumTopicService.saveTopic(topic);

        // Send emails to enabled users
        List<String> toEmails = notificationRepository.findByIsEnable(true).stream()
                .map(Notification::getEmail)
                .collect(Collectors.toList());

        String topicLink = "https://pheocnet.afro.who.int/api/topic/forum"; 
        
        sendEmailsToUsers(toEmails, topic, categoryName, topicLink);

        return ResponseEntity.ok("Topic created successfully");
    }

    
    private void sendEmailsToUsers(List<String> toEmails, ForumTopic topic, String categoryName, String topicLink) {
        String subject = "New Forum Topic Created: " + topic.getTitle();
        for (String toEmail : toEmails) {
            emailService.sendNewTopicEmail(Collections.singletonList(toEmail), subject, topic.getTitle(), categoryName, topicLink);
        }
    }

    @GetMapping("/view/{topicId}")
    public String viewTopic(@PathVariable Long topicId,
                            @RequestParam(defaultValue = "1") int page,
                            Model model,
                            Authentication authentication, 
                            Principal principal) {
    	addUserInfoToModel(model, principal);
        Optional<ForumTopic> topicOptional = forumTopicService.getTopicById(topicId);
        if (topicOptional.isPresent()) {
            ForumTopic topic = topicOptional.get();

            int pageSize = 10;
            Page<ForumComment> commentsPage = getCommentsForTopicPaged(topicId, page, pageSize);

            long numReplies = commentsPage.getTotalElements();
            int numLikes = forumTopicService.getLikesForTopic(topicId);
            boolean isOriginalPoster = checkIfOriginalPoster(topic);

            // Retrieve the ID of the currently authenticated user
            Long currentUserId = null;
            if (authentication != null && authentication.isAuthenticated()) {
                String userEmail = authentication.getName();
                User currentUser = userRepository.findByEmail(userEmail);
                if (currentUser != null) {
                    currentUserId = currentUser.getId();
                }
            }

            model.addAttribute("topic", topic);
            model.addAttribute("currentUserId", currentUserId); // Pass the current user's ID to the view

            if (commentsPage != null && !commentsPage.isEmpty()) {
                model.addAttribute("comments", commentsPage.getContent());
                model.addAttribute("numReplies", numReplies);
                model.addAttribute("numLikes", numLikes);
                model.addAttribute("isOriginalPoster", isOriginalPoster);
                model.addAttribute("currentPage", page);
                model.addAttribute("totalPages", commentsPage.getTotalPages());
                model.addAttribute("previousPage", page > 1 ? page - 1 : 1);
                model.addAttribute("noComments", false);
                model.addAttribute("pageTitle", "Online Forum | Africa PHEOC-Net");
            } else {
                model.addAttribute("numReplies", numReplies);
                model.addAttribute("numLikes", numLikes);
                model.addAttribute("noComments", true);
                model.addAttribute("pageTitle", "Online Forum | Africa PHEOC-Net");
            }

            return "topic-view";
        } else {
            return "topic-not-found";
        }
    }




    public Page<ForumComment> getCommentsForTopicPaged(Long topicId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        return forumCommentRepository.findByTopicId(topicId, pageable);
    }

    private boolean checkIfOriginalPoster(ForumTopic topic) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        String originalPosterUsername = topic.getUser().getUsername(); 
        return currentUsername.equals(originalPosterUsername);
    }
    
    @GetMapping("/attachment/{topicId}")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Long topicId) {
        Optional<ForumTopic> optionalTopic = topicRepository.findById(topicId);
        if (!optionalTopic.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        ForumTopic topic = optionalTopic.get();

        if (topic.getAttachment() == null) {
            return ResponseEntity.notFound().build();
        }

        String attachmentName = topic.getAttachmentName(); // Retrieve attachment name

        try {
            byte[] attachmentBytes = topic.getAttachment().getBytes(1, (int) topic.getAttachment().length());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", attachmentName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(attachmentBytes);
        } catch (SQLException e) {
            // Handle SQLException
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
 // Withdraw topic by changing its status from "Published" to "Withdrawn"
    @PostMapping("/withdraw/{topicId}")
    public ResponseEntity<String> withdrawTopic(@PathVariable Long topicId) {
        Optional<ForumTopic> optionalTopic = forumTopicService.getTopicById(topicId);
        if (!optionalTopic.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        ForumTopic topic = optionalTopic.get();
        if (!topic.getStatus().equals("Published")) {
            return ResponseEntity.badRequest().body("Topic is not published");
        }

        topic.setStatus("Withdrawn");
        forumTopicService.saveTopic(topic);

        return ResponseEntity.ok("Topic withdrawn successfully");
    }

    // Delete topic permanently
    @PostMapping("/delete/{topicId}")
    public ResponseEntity<String> deleteTopic(@PathVariable Long topicId) {
        Optional<ForumTopic> optionalTopic = forumTopicService.getTopicById(topicId);
        if (!optionalTopic.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        forumTopicService.deleteTopic(topicId);

        return ResponseEntity.ok("Topic deleted successfully");
    }

    // Publish topic by changing its status from "Withdrawn" to "Published"
    @PostMapping("/publish/{topicId}")
    public ResponseEntity<String> publishTopic(@PathVariable Long topicId) {
        Optional<ForumTopic> optionalTopic = forumTopicService.getTopicById(topicId);
        if (!optionalTopic.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        ForumTopic topic = optionalTopic.get();
        if (!topic.getStatus().equals("Withdrawn")) {
            return ResponseEntity.badRequest().body("Topic is not withdrawn");
        }

        topic.setStatus("Published");
        forumTopicService.saveTopic(topic);

        return ResponseEntity.ok("Topic published successfully");
    }

    @GetMapping("/list")
    public String listTopics(Model model, Principal principal) {
    	addUserInfoToModel(model, principal);
        List<ForumTopic> topics = topicRepository.findAll();
        List<String> categoryNames = topics.stream()
                                          .map(topic -> {
                                              ForumCategory category = topic.getCategory();
                                              return category != null ? category.getName() : "N/A";
                                          })
                                          .collect(Collectors.toList());
        model.addAttribute("topics", topics);
        model.addAttribute("categoryNames", categoryNames);
        model.addAttribute("pageTitle", "Forum Management | Africa PHEOC-Net");
        return "topic-list"; 
    }
    
    @GetMapping("/forum")
    public String listForum(Model model, Principal principal) {
    	addUserInfoToModel(model, principal);
        List<ForumCategory> categories = forumCategoryService.getAllCategories();
        Map<Long, Long> topicCounts = new HashMap<>(); // Change the map to use category IDs as keys

        // Populate the topicCounts map with the counts for each category
        for (ForumCategory category : categories) {
            Optional<Long> countOptional = forumTopicService.countTopicsByCategoryId(category.getId());
            countOptional.ifPresent(count -> {
                topicCounts.put(category.getId(), count); // Use category IDs as keys
                System.out.println("Category ID: " + category.getId() + ", Topic Count: " + count);
            });
        }

        // Add topic counts to the model
        model.addAttribute("categories", categories);
        model.addAttribute("topicCounts", topicCounts); // Update to use category IDs
        model.addAttribute("pageTitle", "Online Forum | Africa PHEOC-Net");

        return "apps-forum"; 
    }

    @PostMapping("/edit-topic/{topicId}")
    public ResponseEntity<String> editTopic(
            @PathVariable Long topicId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) MultipartFile attachment) {

        // Fetch the topic from the database
        Optional<ForumTopic> optionalTopic = forumTopicService.getTopicById(topicId);
        if (optionalTopic.isPresent()) {
            ForumTopic topic = optionalTopic.get();

            // Update the title and content
            topic.setTitle(title);
            topic.setContent(content);

            // Check if a new attachment is provided and compare attachment names
            if (attachment != null && !attachment.isEmpty()) {
                try {
                    // Virus scanning logic before proceeding with the attachment
                    ClamAVService clamAVService = new ClamAVService();
                    VirusScanResult scanResult = clamAVService.scan(attachment.getInputStream());

                    if (scanResult.getStatus() != VirusScanStatus.PASSED) {
                        // Virus found, reject the attachment
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Attachment contains a virus");
                    }

                    // Convert the byte array to a Blob object
                    Blob attachmentBlob = new javax.sql.rowset.serial.SerialBlob(attachment.getBytes());

                    // Set attachment details
                    topic.setAttachment(attachmentBlob);
                    topic.setAttachmentName(attachment.getOriginalFilename());
                } catch (IOException | SQLException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process attachment");
                }
            }

            // Save the updated topic to the database
            forumTopicService.saveTopic(topic);

            return ResponseEntity.ok("Topic edited successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }




  
    @GetMapping("/edit-topic/{topicId}")
    public String editTopicView(@PathVariable Long topicId, Model model, Principal principal) {
    	addUserInfoToModel(model, principal);
        ForumTopic topic = forumTopicService.getById(topicId);

        if (topic == null) {
            return "topic-not-found";
        }

        model.addAttribute("topic", topic);
        return "edit-topic";
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
