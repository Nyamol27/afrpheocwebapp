package net.pheocnetafr.africapheocnet.forum;
import net.pheocnetafr.africapheocnet.util.ClamAVService;
import net.pheocnetafr.africapheocnet.util.VirusScanResult;
import net.pheocnetafr.africapheocnet.util.VirusScanStatus;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.sql.rowset.serial.SerialBlob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import net.pheocnetafr.africapheocnet.entity.Member;
import net.pheocnetafr.africapheocnet.entity.Notification;
import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.repository.MemberRepository;
import net.pheocnetafr.africapheocnet.repository.NotificationRepository;
import net.pheocnetafr.africapheocnet.repository.UserRepository;
import net.pheocnetafr.africapheocnet.service.EmailService;

import java.security.Principal;
import java.sql.Blob;
import java.sql.Timestamp;
import java.util.HashMap;

@RestController
@RequestMapping("/api/comments")
public class ForumCommentController {

    private final ForumCommentService forumCommentService;
    private final ForumCommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final ForumLikeService forumLikeService;
    
    @Autowired
    private MemberRepository memberRepository;


    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ForumTopicService forumTopicService;

    @Autowired
    public ForumCommentController(ForumCommentService forumCommentService,
                                  ForumCommentRepository commentRepository,
                                  UserRepository userRepository,
                                  EmailService emailService,
                                  TemplateEngine templateEngine,
                                  ForumLikeService forumLikeService) {
        this.forumCommentService = forumCommentService;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.templateEngine = templateEngine;
        this.forumLikeService = forumLikeService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ForumComment> getCommentById(@PathVariable Long id) {
        Optional<ForumComment> comment = forumCommentService.getCommentById(id);
        return comment.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<List<ForumComment>> getCommentsByTopicId(@PathVariable Long topicId) {
        List<ForumComment> comments = forumCommentService.getCommentsByTopicId(topicId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping
    public ResponseEntity<ForumComment> createComment(@RequestBody ForumComment comment) {
        ForumComment createdComment = forumCommentService.createComment(comment);
        return ResponseEntity.ok(createdComment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ForumComment> updateComment(@PathVariable Long id, @RequestBody ForumComment updatedComment) {
        ForumComment comment = forumCommentService.updateComment(id, updatedComment);
        if (comment != null) {
            return ResponseEntity.ok(comment);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        boolean deleted = forumCommentService.deleteComment(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/count/likes/{commentId}")
    public ResponseEntity<Long> getCommentLikeCount(@PathVariable Long commentId) {
        Long likeCount = forumLikeService.getLikeCountForComment(commentId);
        return ResponseEntity.ok(likeCount);
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addComment(
            @RequestParam Long topicId,
            @RequestParam String content,
            @RequestParam(required = false) MultipartFile attachment) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        ForumComment comment = new ForumComment();
        comment.setContent(content);
        comment.setTopicId(topicId);
        comment.setUser(currentUser);
        comment.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        comment.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        // Set attachment if provided
        if (attachment != null && !attachment.isEmpty()) {
            try {
                // Virus scanning logic before proceeding with the attachment
                ClamAVService clamAVService = new ClamAVService();
                VirusScanResult scanResult = clamAVService.scan(attachment.getInputStream());

                if (scanResult.getStatus() != VirusScanStatus.PASSED) {
                    // Virus found, reject the attachment
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                }

                // Set attachment details
                comment.setAttachment(attachment.getBytes());
                comment.setAttachmentContentType(attachment.getContentType());
                comment.setAttachmentName(attachment.getOriginalFilename());

                // Create Blob from attachment data and set it to attachmentData
                Blob blob = new SerialBlob(attachment.getBytes());
                comment.setAttachmentData(blob);
            } catch (IOException | SQLException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        }

        String firstName = currentUser.getFirstName();
        String lastName = currentUser.getLastName();

        // Construct the initials using the first letter of the first name and last name
        String initials = (firstName != null && lastName != null) ?
                firstName.substring(0, 1).toUpperCase() + lastName.substring(0, 1).toUpperCase() :
                "UU";

        // Save the comment to the database using the repository
        commentRepository.save(comment);

        // Send email notifications to enabled users
        List<String> toEmails = notificationRepository.findByIsEnable(true).stream()
                .map(Notification::getEmail)
                .collect(Collectors.toList());

        ForumTopic topic = forumTopicService.findById(topicId);
        String categoryName = topic.getCategory().getName();

        sendEmailsToUsers(toEmails, topic, currentUser.getFirstName(), currentUser.getLastName(), content, categoryName);

        // Construct the response body with necessary information
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("userName", currentUser.getFirstName() + " " + currentUser.getLastName());
        responseBody.put("createdAt", comment.getCreatedAt());
        responseBody.put("content", content);
        responseBody.put("initials", initials);
        responseBody.put("attachmentName", attachment != null && !attachment.isEmpty() ? attachment.getOriginalFilename() : null);
        responseBody.put("attachment", attachment != null && !attachment.isEmpty());

        return ResponseEntity.ok(responseBody);
    }


    private void sendEmailsToUsers(List<String> toEmails, ForumTopic topic, String firstName, String lastName, String content, String categoryName) {
        for (String toEmail : toEmails) {
            String subject = "New Comment on Forum Topic: " + topic.getTitle();
            emailService.sendNewCommentEmail(
                Collections.singletonList(toEmail),
                subject,
                topic.getTitle(),
                categoryName,
                firstName,
                lastName,
                content,
                "https://pheocnet.afro.who.int/api/topic/forum" 
            );
        }
    }

    @PostMapping("/edit/{commentId}")
    public ResponseEntity<String> editComment(
            @PathVariable Long commentId,
            @RequestParam String content,
            @RequestParam(required = false) MultipartFile attachment) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // Retrieve the existing comment from the database
        Optional<ForumComment> optionalComment = commentRepository.findById(commentId);
        if (!optionalComment.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment not found");
        }

        ForumComment comment = optionalComment.get();

        // Check if the current user is the author of the comment
        if (!comment.getUser().equals(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not the author of the comment");
        }

        // Update the content of the comment
        comment.setContent(content);
        comment.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        // Update the attachment if a new one is provided
        if (attachment != null && !attachment.isEmpty()) {
            try {
                // Virus scanning logic before proceeding with the attachment
                ClamAVService clamAVService = new ClamAVService();
                VirusScanResult scanResult = clamAVService.scan(attachment.getInputStream());

                if (scanResult.getStatus() != VirusScanStatus.PASSED) {
                    // Virus found, reject the attachment
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Attachment contains a virus");
                }

                // Set attachment details
                comment.setAttachment(attachment.getBytes());
                comment.setAttachmentContentType(attachment.getContentType());
                comment.setAttachmentName(attachment.getOriginalFilename());

                // Create Blob from attachment data and set it to attachmentData
                Blob blob = new SerialBlob(attachment.getBytes());
                comment.setAttachmentData(blob);
            } catch (IOException | SQLException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process attachment");
            }
        }

        // Save the updated comment to the database
        commentRepository.save(comment);

        return ResponseEntity.ok("Comment updated successfully");
    }
    
    
    @GetMapping("/attachment/{commentId}")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Long commentId) {
        Optional<ForumComment> optionalComment = commentRepository.findById(commentId);
        if (!optionalComment.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        ForumComment comment = optionalComment.get();

        if (comment.getAttachment() == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", comment.getAttachmentName());

        return new ResponseEntity<>(comment.getAttachment(), headers, HttpStatus.OK);
    }
    
    @GetMapping("/comments")
    public String viewAllComments(Model model,Principal principal) {
    	addUserInfoToModel(model, principal);
        List<ForumComment> comments = forumCommentService.getAllComments();
        model.addAttribute("comments", comments);
        model.addAttribute("pageTitle", "Forum Management | Africa PHEOC-Net");
        return "comment-list";
    }
    

    @PostMapping("/delete/{commentId}")
    public ResponseEntity<String> deleteCommentById(@PathVariable Long commentId) {
        boolean deleted = forumCommentService.deleteCommentById(commentId);
        if (deleted) {
            return ResponseEntity.ok("Comment deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete comment with ID: " + commentId);
        }
    }

    @GetMapping("/list-topic/{topicId}")
    public String listCommentsByTopicId(@PathVariable Long topicId, Model model, Principal principal) {
    	addUserInfoToModel(model, principal);
        List<ForumComment> comments = forumCommentService.findCommentsByTopicId(topicId);
        
        // Add comment details with author's first name and last name to the model
        model.addAttribute("comments", comments);
        model.addAttribute("pageTitle", "Forum Management | Africa PHEOC-Net");

        return "topic-comment-list";
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

