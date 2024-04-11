package net.pheocnetafr.africapheocnet.controller;

import net.pheocnetafr.africapheocnet.entity.Invitation;
import net.pheocnetafr.africapheocnet.entity.Member;
import net.pheocnetafr.africapheocnet.service.EmailService;
import net.pheocnetafr.africapheocnet.service.InvitationService;
import net.pheocnetafr.africapheocnet.service.MemberService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.Context;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

@Controller
@RequestMapping("/invitations")
public class InvitationController {

    @Autowired
    private InvitationService invitationService;

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private MemberService memberService;

    @PostMapping("/send")
    public ResponseEntity<String> sendInvitation(@RequestParam("email") String receiverEmail, 
                                                 @RequestParam("name") String receiverName,
                                                 HttpServletRequest request) {
        try {
            // Check if the receiver email has already been invited
            if (invitationService.isEmailAlreadyInvited(receiverEmail)) {
                return ResponseEntity.ok("The receiver has already been invited.");
            }

            // Retrieve the currently authenticated user's details
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String senderEmail = userDetails.getUsername(); 

            // Fetch corresponding member details from the database
            Member senderMember = memberService.getMemberByEmail(senderEmail);
            if (senderMember == null) {
                return ResponseEntity.badRequest().body("Sender member details not found.");
            }

            // Extract sender's first and last name
            String[] senderNameParts = senderMember.getFirstName().split(" ");
            senderMember.setFirstName(senderNameParts[0]);
            if (senderNameParts.length > 1) {
                senderMember.setLastName(senderNameParts[1]);
            }

            // Generate invitation token
            String invitationToken = generateInvitationToken();

            // Set up the invitation request
            Invitation invitation = new Invitation();
            invitation.setReceiverEmail(receiverEmail);
            invitation.setInvitationToken(invitationToken);
            invitation.setSenderEmail(senderEmail); 
            invitation.setReceiverName(receiverName);
            invitation.setStatus("Pending");
            invitation.setSenderName(senderMember.getFirstName()); 

            // Save the invitation request
            invitationService.createInvitation(invitation);

            // Send an invitation email with the link containing the invitation token
            String inviteLink = "https://yourapp.com/register?token=" + invitationToken;

            // Create a Thymeleaf context with variables for the email template
            Context context = new Context();
            context.setVariable("senderName", senderMember.getFirstName()); 
            context.setVariable("inviteLink", inviteLink);
            context.setVariable("receiverName", receiverName);

            // Send the invitation email
            emailService.sendInvitationEmail(receiverEmail, "Invitation to Join", "invitation-email", context);

            return ResponseEntity.ok("Invitation sent successfully.");
        } catch (Exception e) {
            // Handle the exception as needed (log or notify admin)
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send the invitation.");
        }
    }



    // Generate a unique invitation token (you can use UUID or any other method)
    private String generateInvitationToken() {
        return UUID.randomUUID().toString();
    }
}
