package net.pheocnetafr.africapheocnet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import net.pheocnetafr.africapheocnet.entity.TwgMembershipRequest;
import net.pheocnetafr.africapheocnet.entity.WorkingGroup;
import net.pheocnetafr.africapheocnet.repository.TwgMembershipRequestRepository;
import net.pheocnetafr.africapheocnet.repository.WorkingGroupRepository;
import net.pheocnetafr.africapheocnet.util.MembershipRequestNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
public class TwgMembershipRequestService {
    private final TwgMembershipRequestRepository twgMembershipRequestRepository;

    @Autowired
    private EmailService emailService; 

    @Autowired
    public TwgMembershipRequestService(TwgMembershipRequestRepository twgMembershipRequestRepository) {
        this.twgMembershipRequestRepository = twgMembershipRequestRepository;
    }

    public String applyForMembership(TwgMembershipRequest request) {
        // Check if the user is already a member of the requested group
        List<TwgMembershipRequest> existingRequests = twgMembershipRequestRepository.findByEmailAndTwgAndStatus(
            request.getEmail(), request.getTwg(), "Approved"
        );

        if (!existingRequests.isEmpty()) {
            return "You are already a member of this working group.";
        }

        // Set the status as "Pending" and save the request
        request.setStatus("Pending");
        twgMembershipRequestRepository.save(request);
        
        // Send an email notification to the user
        sendMembershipRequestEmail(request.getEmail(), "Your Membership Request", "Your membership request has been received.");

        return "Membership request submitted.";
    }

    public List<TwgMembershipRequest> getAllMembershipRequests() {
        return twgMembershipRequestRepository.findAll();
    }

    public boolean approveMembershipRequest(Long requestId) {
        Optional<TwgMembershipRequest> request = twgMembershipRequestRepository.findById(requestId);
        if (request.isPresent()) {
            request.get().setStatus("Approved");
            twgMembershipRequestRepository.save(request.get());

            // Send an email notification to the user
            sendMembershipApprovalEmail(request.get().getEmail(), "Your Membership Request Approved", "Your membership request has been approved.");
            return true;
        }
        return false;
    }

    public boolean rejectMembershipRequest(Long requestId) {
        Optional<TwgMembershipRequest> request = twgMembershipRequestRepository.findById(requestId);
        if (request.isPresent()) {
            twgMembershipRequestRepository.delete(request.get());

            // Send an email notification to the user
            sendMembershipRejectionEmail(request.get().getEmail(), "Your Membership Request Rejected", "Your membership request has been rejected.");
            return true;
        }
        return false;
    }
    
    
    public boolean revokeMembershipRequest(Long requestId) {
        Optional<TwgMembershipRequest> request = twgMembershipRequestRepository.findById(requestId);
        if (request.isPresent()) {
            twgMembershipRequestRepository.delete(request.get());

            // Send an email notification to the user
            sendMembershipRejectionEmail(request.get().getEmail(), "Your Membership Revoked", "Your membership has been revoked.");
            return true;
        }
        return false;
    }

    public List<TwgMembershipRequest> getMembersByGroup(String group) {
        return twgMembershipRequestRepository.findByTwgAndStatus(group, "Approved");
    }

    public boolean withdrawMemberFromGroup(Long memberId) {
        Optional<TwgMembershipRequest> member = twgMembershipRequestRepository.findById(memberId);
        if (member.isPresent() && member.get().getStatus().equals("Approved")) {
            twgMembershipRequestRepository.delete(member.get());
            return true;
        }
        return false;
    }
    
    public String getMembershipStatus(String email, Long workingGroupId) {
        // Convert workingGroupId to a string
        String workingGroupIdStr = String.valueOf(workingGroupId);

        // Find membership requests with the provided email, working group ID, and approved status
        List<TwgMembershipRequest> approvedMembershipRequests = twgMembershipRequestRepository.findByEmailAndWorkingGroup_IdAndStatus(email, workingGroupId, "Approved");

        // Find membership requests with the provided email, working group ID, and pending status
        List<TwgMembershipRequest> pendingMembershipRequests = twgMembershipRequestRepository.findByEmailAndWorkingGroup_IdAndStatus(email, workingGroupId, "Pending");

        // Check if any approved membership requests were found
        if (!approvedMembershipRequests.isEmpty()) {
            return "You are already a member of this working group.";
        } 
        // Check if any pending membership requests were found
        else if (!pendingMembershipRequests.isEmpty()) {
            return "Your membership request for this working group is pending approval.";
        } 
        // If no approved or pending requests were found, return message indicating not a member
        else {
            return "You are not a member of this working group.";
        }
    }
    public void cancelMembershipRequest(Long requestId) {
        // Check if the membership request exists
        Optional<TwgMembershipRequest> optionalMembershipRequest = twgMembershipRequestRepository.findById(requestId);
        if (optionalMembershipRequest.isPresent()) {
            // Delete the membership request from the database
            twgMembershipRequestRepository.delete(optionalMembershipRequest.get());
        } else {
            throw new MembershipRequestNotFoundException("Membership request with ID " + requestId + " not found");
        }
    }

    public void createMembershipRequest(TwgMembershipRequest membershipRequest) {
        // Set the status to "New" when creating the request
        membershipRequest.setStatus("Pending");
        twgMembershipRequestRepository.save(membershipRequest);
        
        // Send an email notification to the user
        sendMembershipRequestEmail(membershipRequest.getEmail(), "Your Membership Request", "Your membership request has been received.");
    }

    // Email notification methods
    private void sendMembershipRequestEmail(String toEmail, String subject, String content) {
        emailService.sendMembershipRequestEmail(toEmail, subject, content);
    }

    private void sendMembershipApprovalEmail(String toEmail, String subject, String content) {
        emailService.sendMembershipApprovalEmail(toEmail, subject, content);
    }

    private void sendMembershipRejectionEmail(String toEmail, String subject, String content) {
        emailService.sendMembershipRejectionEmail(toEmail, subject, content);
    }
    
    public void leaveGroup(Long requestId) {
       
        twgMembershipRequestRepository.deleteById(requestId);
    }

    public List<TwgMembershipRequest> getMembershipRequestsByEmail(String email) {
      
        return twgMembershipRequestRepository.findByEmail(email);
    }
    
    public long getTotalMembers(String twgName) {
        // Call the repository method to count the total number of members with status "Approved"
        return twgMembershipRequestRepository.countByTwgAndStatus(twgName, "Approved");
    }
   
    public List<TwgMembershipRequest> getMembersByTWG(String twgName) {
        return twgMembershipRequestRepository.findByTwg(twgName);
    }
    
    public Page<TwgMembershipRequest> getMembersPageByTWG(String twgName, PageRequest pageRequest) {
        // Implement the logic to fetch members by TWG using your repository
        return twgMembershipRequestRepository.findByTwg(twgName, pageRequest);
    }
}
