package net.pheocnetafr.africapheocnet.controller;

import net.pheocnetafr.africapheocnet.entity.Member;
import net.pheocnetafr.africapheocnet.entity.TwgMembershipRequest;
import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.entity.WorkingGroup;
import net.pheocnetafr.africapheocnet.repository.MemberRepository;
import net.pheocnetafr.africapheocnet.repository.UserRepository;
import net.pheocnetafr.africapheocnet.service.MemberService;
import net.pheocnetafr.africapheocnet.service.TwgMembershipRequestService;
import net.pheocnetafr.africapheocnet.service.WorkingGroupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/twg-membership-requests")
public class TwgMembershipRequestController {
	
	@Autowired
	private UserRepository userRepository;
	 @Autowired
	    private MemberRepository memberRepository;

    private final TwgMembershipRequestService twgMembershipRequestService;
    private final WorkingGroupService workingGroupService;

    @Autowired
    public TwgMembershipRequestController(TwgMembershipRequestService twgMembershipRequestService, WorkingGroupService workingGroupService) {
        this.twgMembershipRequestService = twgMembershipRequestService;
        this.workingGroupService = workingGroupService;
    }

    @GetMapping("/list")
    public String getAllMembershipRequests(Model model, Principal principal) {
    	addUserInfoToModel(model, principal);
        // Retrieve all membership requests
        List<TwgMembershipRequest> membershipRequests = twgMembershipRequestService.getAllMembershipRequests();

        // Retrieve all members
        List<Member> members = memberService.getAllMembers(); 

        // Add additional information to the membership requests
        for (TwgMembershipRequest request : membershipRequests) {
            for (Member member : members) {
                // Perform null checks for email in both TwgMembershipRequest and Member
                if (request.getEmail() != null && member.getEmail() != null) {
                   
                    // Compare emails
                    if (member.getEmail().equals(request.getEmail())) {
                        // Populate additional information from the Member entity to the TwgMembershipRequest entity
                        request.setPosition(member.getPosition());
                        request.setOrganization(member.getOrganization());
                        request.setNationality(member.getNationality());
                        request.setFirstName(member.getFirstName());
                        request.setLasttName(member.getLastName());
                        
                        // Break the loop once the information is populated
                        break; 
                    }
                }
            }
        }

        // Add the populated membership requests and page title to the model
        model.addAttribute("membershipRequests", membershipRequests);
        model.addAttribute("pageTitle", "Membership Requests | Africa PHEOC-Net");

        return "twgMembershipRequest-list";
    }

    
    @PostMapping("/join")
    public ResponseEntity<String> createMembershipRequest(@RequestParam("working_group_id") Long workingGroupId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        // Fetch user details to get first name and last name
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String firstName = ""; // Retrieve user's first name from userDetails
        String lastName = ""; // Retrieve user's last name from userDetails

        // Retrieve first name and last name from userDetails based on your UserDetails implementation
        if (userDetails != null) {
            String username = userDetails.getUsername();
            User user = userRepository.findByEmail(username);
            if (user != null) {
                firstName = user.getFirstName();
                lastName = user.getLastName();
            }
        }

        // Check if the working group ID is null or empty
        if (workingGroupId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Working group ID is missing.");
        }

        // Fetch the working group from the database based on the ID
        Optional<WorkingGroup> optionalWorkingGroup = workingGroupService.getWorkingGroupById(workingGroupId);

     // Check if the optional contains a value
        if (optionalWorkingGroup.isPresent()) {
            // Extract the WorkingGroup object from the Optional
            WorkingGroup workingGroup = optionalWorkingGroup.get();

            // Check if the user is already a member of the working group
            String membershipStatus = twgMembershipRequestService.getMembershipStatus(userEmail, workingGroupId);
            if (membershipStatus.equals("You are already a member of this working group.")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(membershipStatus);
            } else if (membershipStatus.equals("Your membership request for this working group is pending approval.")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(membershipStatus);
            } else {
                // Create the membership request
                TwgMembershipRequest membershipRequest = new TwgMembershipRequest();
                membershipRequest.setWorkingGroup(workingGroup); // Set the working group
                membershipRequest.setEmail(userEmail); // Set the user's email
                
                // Combine first name and last name and set it to the name field
                String fullName = firstName + " " + lastName;
                membershipRequest.setName(fullName);
                
                // Set the twg (working group name)
                String workingGroupName = workingGroup.getName();
                membershipRequest.setTwg(workingGroupName);

                // Save the membership request
                twgMembershipRequestService.createMembershipRequest(membershipRequest);

                return ResponseEntity.status(HttpStatus.OK).body("Your membership request has been submitted.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Working group not found for the provided ID.");
        }

    }




    @PostMapping("/approve/{id}")
    public ResponseEntity<String> approveMembershipRequest(@PathVariable Long id) {
        twgMembershipRequestService.approveMembershipRequest(id);
        return ResponseEntity.status(HttpStatus.OK).body("Membership request has been approved.");
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<String> rejectMembershipRequest(@PathVariable Long id) {
        twgMembershipRequestService.rejectMembershipRequest(id);
        return ResponseEntity.status(HttpStatus.OK).body("Membership request has been rejected.");
    }
    
    @PostMapping("/revoke/{id}")
    public ResponseEntity<String> revokeMembershipRequest(@PathVariable Long id) {
        twgMembershipRequestService.revokeMembershipRequest(id);
        return ResponseEntity.status(HttpStatus.OK).body("Membership request has been rejected.");
    }

    @PostMapping("/leave")
    public ResponseEntity<String> leaveGroup(@RequestParam Long requestId) {
        twgMembershipRequestService.leaveGroup(requestId);
        return ResponseEntity.status(HttpStatus.OK).body("You have left the group.");
    }

    @GetMapping("/membership-requests")
    public String getMembershipRequests(Model model, Principal principal) {
    	addUserInfoToModel(model, principal);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        List<TwgMembershipRequest> membershipRequests = twgMembershipRequestService.getMembershipRequestsByEmail(userEmail);

        // Create a list to hold the associated working groups
        List<WorkingGroup> associatedWorkingGroups = new ArrayList<>();

        // Fetch the associated working groups for each membership request
        for (TwgMembershipRequest membershipRequest : membershipRequests) {
            WorkingGroup workingGroup = membershipRequest.getWorkingGroup();
            associatedWorkingGroups.add(workingGroup);
        }

        model.addAttribute("pageTitle", "Technical Working Groups | Africa PHEOC-Net");
        model.addAttribute("membershipRequests", membershipRequests);
        model.addAttribute("associatedWorkingGroups", associatedWorkingGroups);

        return "apps-working-group";
    }
    
    @PostMapping("/cancel")
    public ResponseEntity<String> cancelMembershipRequest(@RequestParam Long requestId) {
        // Check if the requestId is valid
        if (requestId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request ID is missing.");
        }

        // Delete the membership request from the database
        try {
            twgMembershipRequestService.cancelMembershipRequest(requestId);
            return ResponseEntity.status(HttpStatus.OK).body("Membership request has been canceled successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to cancel the membership request.");
        }
    }
    
    @GetMapping("/view/{twgName}")
    public String viewMembershipRequest(@PathVariable String twgName, Model model, Principal principal) {
    	addUserInfoToModel(model, principal);
        
        long totalMembers = twgMembershipRequestService.getTotalMembers(twgName);
        
       
        model.addAttribute("twgName", twgName);
        model.addAttribute("totalMembers", totalMembers);
        model.addAttribute("pageTitle", "Technical Working Groups | Africa PHEOC-Net");
        
        return "view-twg"; 
    }
  
    
    @Autowired
    private MemberService memberService;

    @GetMapping("/members/view/{twg}")
    public String viewMembersOfTWG(@PathVariable("twg") String twgName,
                                    @RequestParam(name = "page", defaultValue = "1") int page,
                                    @RequestParam(name = "size", defaultValue = "10") int size,
                                    Model model, Principal principal) {
        Page<TwgMembershipRequest> membersPage = twgMembershipRequestService.getMembersPageByTWG(twgName, PageRequest.of(page - 1, size));

        if (membersPage != null && !membersPage.isEmpty()) {
            List<String> initialsList = new ArrayList<>();
            List<Member> members = new ArrayList<>();

            for (TwgMembershipRequest member : membersPage.getContent()) {
                String email = member.getEmail();
                Member profile = memberService.getMemberByEmail(email);
                String name = member.getName();
                String[] names = name.split("\\s+");
                String initials = String.valueOf(names[0].charAt(0)) + String.valueOf(names[1].charAt(0));
                initialsList.add(initials);
                members.add(profile);
            }
            addUserInfoToModel(model, principal);
            model.addAttribute("members", members);
            model.addAttribute("twgName", twgName);
            model.addAttribute("pageTitle", "Members of TWG: " + twgName);
            model.addAttribute("contactInitials", initialsList);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", membersPage.getTotalPages());
            model.addAttribute("totalMembers", membersPage.getTotalElements());

            return "view-twg-members"; 
        } else {
            model.addAttribute("message", "No members found for TWG: " + twgName);
            return "error"; 
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
