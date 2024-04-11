
package net.pheocnetafr.africapheocnet.controller;
import net.pheocnetafr.africapheocnet.util.ClamAVService;
import net.pheocnetafr.africapheocnet.util.VirusScanResult;
import net.pheocnetafr.africapheocnet.util.VirusScanStatus;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.sql.Blob;
import net.pheocnetafr.africapheocnet.entity.Member;
import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.repository.MemberRepository;
import net.pheocnetafr.africapheocnet.repository.UserRepository;
import net.pheocnetafr.africapheocnet.service.MemberService;
import net.pheocnetafr.africapheocnet.service.UserService;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Controller
@RequestMapping("/members")
public class MembershipController {
	
	private static final Logger logger = LoggerFactory.getLogger(MembershipController.class);
    @Autowired
    private MemberService memberService;
    
    @Autowired
    private UserRepository userRepository;
    

    @Autowired
    public MembershipController(MemberService memberService, MemberRepository memberRepository) {
        this.memberService = memberService;
        this.memberRepository = memberRepository;
    }
    private final MemberRepository memberRepository;
    
    private void convertPhotoToBase64(Member member) {
        byte[] photoBytes = member.getPhoto();
        if (photoBytes != null) {
            String base64Photo = Base64.getEncoder().encodeToString(photoBytes);
            member.setBase64Photo(base64Photo);
        }
    }

    @GetMapping("/list")
    public String viewAllMembers(
            @RequestParam(name = "page", defaultValue = "1") int currentPage,
            @RequestParam(name = "size", defaultValue = "10") int pageSize,
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "expertise", required = false) String expertise,
            Model model,
            Authentication authentication,
            Principal principal
    ) {
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
         
        int totalMembers;
        List<Member> members;
        boolean isSearchResult = false;


        if (query != null && !query.isEmpty()) {
            System.out.println("Searching members with query: " + query);
            members = memberService.searchMembers(query);
            totalMembers = members.size(); // total number of search results
            isSearchResult = true;
        } else if (expertise != null && !expertise.isEmpty()) {
            System.out.println("Searching members with expertise: " + expertise);
            members = memberService.findByExpertise(expertise);
            totalMembers = members.size(); // total number of expertise-based results
        } else {
            System.out.println("Fetching all members");
            members = memberService.getMembersByPage(currentPage, pageSize);
            totalMembers = memberService.getTotalMembersCount(); // total number of all members
        }

        System.out.println("Total Members: " + totalMembers);

        // Paginate the members list
        int totalPages = (int) Math.ceil((double) totalMembers / pageSize);
        int startEntry = Math.min((currentPage - 1) * pageSize + 1, totalMembers);
        int endEntry = Math.min(currentPage * pageSize, totalMembers);
        String pageUrl = "/members/list";

        // Ensure totalPages, startEntry, and endEntry are not null
        if (totalPages == 0) {
            totalPages = 1; // Default to 1 if totalPages is 0
        }
        if (startEntry == 0) {
            startEntry = 1; // Default to 1 if startEntry is 0
        }
        if (endEntry == 0) {
            endEntry = totalMembers; // Default to totalMembers if endEntry is 0
        }


        // Extract members for the current page
        int startIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalMembers);
        List<Member> currentMembers = new ArrayList<>();

        if (!members.isEmpty()) {
            currentMembers = members.subList(startIndex, endIndex);
        }

        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));

        model.addAttribute("members", currentMembers);
        model.addAttribute("totalMembers", totalMembers);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startEntry", startEntry);
        model.addAttribute("endEntry", endEntry);
        model.addAttribute("totalEntries", totalMembers);
        model.addAttribute("pageUrl", pageUrl);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("pageTitle", "Members List | Africa PHEOC-Net");
        model.addAttribute("isSearchResult", isSearchResult);

        return "member-list";
    }


    @GetMapping("/view/{email}")
    public String viewMemberProfile(@PathVariable String email, Model model,Principal principal) {
    	addUserInfoToModel(model, principal);
        try {
            Member member = memberService.getMemberByEmail(email);
            if (member != null) {
                convertPhotoToBase64(member);
                model.addAttribute("member", member);
                return "member-profile";
            } else {
                System.out.println("Member not found for email: " + email);
                return "error";
            }
        } catch (Exception e) {
            System.out.println("Error fetching member profile for email: " + email);
            e.printStackTrace();
            return "error";
        }
    }

    @GetMapping("/{email}/edit")
    public String editMemberProfileForm(@PathVariable String email, Model model) {
        Member member = memberService.getMemberByEmail(email);
        if (member != null) {
            model.addAttribute("member", member);
            return "edit-member-profile";
        } else {
            return "error-page";
        }
    }

    @PostMapping("/{email}/edit")
    public String updateMemberProfile(@PathVariable String email, @ModelAttribute Member updatedMember) {
        Member existingMember = memberService.updateMemberProfile(email, updatedMember);
        if (existingMember != null) {
            return "redirect:/members/" + email;
        } else {
            return "error-page";
        }
    }

    @GetMapping("/{email}/delete")
    public String deleteMember(@PathVariable String email) {
        memberService.deleteMemberByEmail(email);
        return "redirect:/members";
    }

    @GetMapping("/nationality/{nationality}")
    public String viewMembersByNationality(@PathVariable String nationality, Model model) {
        List<Member> members = memberService.findByNationality(nationality);
        model.addAttribute("members", members);
        model.addAttribute("filter", "Nationality: " + nationality);
        return "member-list";
    }

   

    @GetMapping("/search")
    public ResponseEntity<List<Member>> searchMembers(
            @RequestParam(name = "query") String query,
            @RequestParam(name = "page", defaultValue = "1") int currentPage,
            @RequestParam(name = "size", defaultValue = "10") int pageSize
    ) {
        logger.info("Received search query: {}", query);
        List<Member> searchResults = memberService.searchMembers(query);
        int totalResults = searchResults.size();

        // Paginate the search results
        int startIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalResults);
        List<Member> currentSearchResults = searchResults.subList(startIndex, endIndex);

        // Check if there are search results
        if (currentSearchResults.isEmpty()) {
            // Return a 404 (NOT FOUND) status code if no members are found
            return ResponseEntity.notFound().build();
        } else {
            // Return the search results with status code 200 (OK)
            return ResponseEntity.ok(currentSearchResults);
        }
    }

    @GetMapping("/filter")
    public String filterMembers(@RequestParam(name = "expertise") String expertise, Model model) {
        List<Member> filteredMembers = memberService.findByExpertise(expertise);
        model.addAttribute("members", filteredMembers);
        model.addAttribute("filter", "Expertise: " + expertise);
        model.addAttribute("isSearchResult", false);
        return "member-list";
    }
    @GetMapping(value = "/export", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    
    public ResponseEntity<byte[]> exportData() {
        List<Member> members = memberRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            // Create a new sheet
            Sheet sheet = workbook.createSheet("Members");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"First Name", "Last Name", "Email", "Gender", "Nationality", "Profession", "Organization", "Position", "Expertise", "Language", "Enrollment"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Populate data rows
            int rowNum = 1;
            for (Member member : members) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(member.getFirstName());
                row.createCell(1).setCellValue(member.getLastName());
                row.createCell(2).setCellValue(member.getEmail());
                row.createCell(3).setCellValue(member.getGender());
                row.createCell(4).setCellValue(member.getNationality());
                row.createCell(5).setCellValue(member.getProfession());
                row.createCell(6).setCellValue(member.getOrganization());
                row.createCell(7).setCellValue(member.getPosition());
                row.createCell(8).setCellValue(member.getExpertise());
                row.createCell(9).setCellValue(member.getLanguage());
                row.createCell(10).setCellValue(member.getEnrollment());
            }

            // Convert workbook to byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            workbook.write(byteArrayOutputStream);

            // Create ResponseEntity with byte array and appropriate headers
            HttpHeaders header = new HttpHeaders();
            header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=members_export.xlsx");

            return ResponseEntity
                    .ok()
                    .headers(header)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(byteArrayOutputStream.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    
    @GetMapping("/profile")
    public String showProfileForm(Model model, Authentication authentication, Principal principal) {
    	addUserInfoToModel(model, principal);
        try {
            // Fetch the logged-in member's email from the authentication object
            String email = authentication.getName();
            
            // Fetch the member's profile using the email
            Member member = memberService.getMemberByEmail(email);
            
            if (member != null) {
                // Convert photo to Base64 if not already done
                convertPhotoToBase64(member);
                
                // Add member object to the model
                model.addAttribute("member", member);
                
                
                model.addAttribute("profileEditable", true); 
                model.addAttribute("pageTitle", "Member Profile | Africa PHEOC-Net");
                
                return "view-profile"; 
            } else {
                // Handle the case where member is not found
                System.out.println("Member not found for email: " + email);
                return "error"; 
            }
        } catch (Exception e) {
            // Handle any exceptions that may occur
            System.out.println("Error fetching member profile for logged-in member");
            e.printStackTrace();
            return "error"; 
        }
    }
    
   
    @PostMapping("/update-profile")
    public ResponseEntity<String> updateProfile(@RequestParam("userData") String userData,
                                                @RequestParam(value = "photo", required = false) MultipartFile photo) {
        try {
            // Retrieve the currently authenticated user's email from the principal
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = null;
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
            }

            // Check if userEmail is null or empty before continuing
            if (userEmail == null || userEmail.isEmpty()) {
                return ResponseEntity.badRequest().body("User email not found");
            }

            // Convert JSON string to Member object
            ObjectMapper objectMapper = new ObjectMapper();
            Member memberToUpdate = objectMapper.readValue(userData, Member.class);

            // Fetch the existing member from the database using the retrieved email
            Member existingMember = memberService.findByEmail(userEmail);

            if (existingMember == null) {
                return ResponseEntity.badRequest().body("Member not found");
            }

            // Update the necessary details of the existing member with the new data
            existingMember.setFirstName(memberToUpdate.getFirstName());
            existingMember.setLastName(memberToUpdate.getLastName());
            existingMember.setGender(memberToUpdate.getGender());
            existingMember.setNationality(memberToUpdate.getNationality());
            existingMember.setPosition(memberToUpdate.getPosition());
            existingMember.setOrganization(memberToUpdate.getOrganization());
            existingMember.setExpertise(memberToUpdate.getExpertise());
            existingMember.setProfession(memberToUpdate.getProfession());
            existingMember.setBio(memberToUpdate.getBio());

            // Scan the photo file for viruses if provided
            if (photo != null && !photo.isEmpty()) {
                // Initialize ClamAV service
                ClamAVService clamAVService = new ClamAVService();

                // Check if ClamAV is reachable
                if (!clamAVService.ping()) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to connect to ClamAV service.");
                }

                // Perform virus scan
                VirusScanResult scanResult = clamAVService.scan(photo.getInputStream());

              
             // Check scan result
                if (scanResult.getStatus() != VirusScanStatus.PASSED) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Virus found in attachment: " + scanResult.getSignature());
                }

            }

            // Update member's profile with data from the request
            memberService.updateProfile(existingMember, photo);

            return ResponseEntity.ok("Profile updated successfully!");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating profile");
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

   
    

    
}
