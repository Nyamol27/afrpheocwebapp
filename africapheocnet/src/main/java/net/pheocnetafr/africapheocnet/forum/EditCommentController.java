package net.pheocnetafr.africapheocnet.forum;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import net.pheocnetafr.africapheocnet.entity.Member;
import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.repository.MemberRepository;
import net.pheocnetafr.africapheocnet.repository.UserRepository;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.sql.Blob;
import java.util.Base64;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import net.pheocnetafr.africapheocnet.util.ClamAVService;
import net.pheocnetafr.africapheocnet.util.VirusScanResult;
import net.pheocnetafr.africapheocnet.util.VirusScanStatus;
@Controller
@RequestMapping("/api/forum/comments")
public class EditCommentController {

    @Autowired
    private ForumCommentRepository commentRepository;
    
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/edit-comment/{commentId}")
    public String showEditCommentPage(@PathVariable Long commentId, Model model, Principal principal) {
    	addUserInfoToModel(model, principal);
        
        ForumComment comment = commentRepository.findById(commentId).orElse(null);
        
        
        model.addAttribute("comment", comment);
        
        
        return "edit-comment";
    }
 
    
    @PostMapping("/edit-comment/{commentId}")
    public ResponseEntity<String> editComment(
        @PathVariable Long commentId,
        @RequestParam String content,
        @RequestParam(required = false) MultipartFile attachment) {
        
        // Fetch the comment from the database
        Optional<ForumComment> optionalComment = commentRepository.findById(commentId);
        if (optionalComment.isPresent()) {
            ForumComment comment = optionalComment.get();

            // Update the content
            comment.setContent(content);

            // Check if a new attachment is provided and compare attachment names
            if (attachment != null && !attachment.isEmpty()) {
                try {
                    // Check if the attachment name is different from the current attachment
                    if (!attachment.getOriginalFilename().equals(comment.getAttachmentName())) {
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
                        Blob blob = new javax.sql.rowset.serial.SerialBlob(attachment.getBytes());
                        comment.setAttachmentData(blob);
                    }
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process attachment");
                }
            }

            // Save the updated comment to the database
            commentRepository.save(comment);

            return ResponseEntity.ok("Comment edited successfully");
        } else {
            return ResponseEntity.notFound().build();
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
