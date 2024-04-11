package net.pheocnetafr.africapheocnet.service;

import net.pheocnetafr.africapheocnet.entity.Member;
import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.repository.MemberRepository;
import net.pheocnetafr.africapheocnet.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final EmailService emailService; 

    @Autowired
    public MemberService(MemberRepository memberRepository, UserRepository userRepository, EmailService emailService) {
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId).orElse(null);
    }

    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public Member createMember(Member member) {
        return memberRepository.save(member);
    }

    public Member updateProfilePicture(Long memberId, byte[] newProfilePicture) {
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member != null) {
            member.setPhoto(newProfilePicture);
            return memberRepository.save(member);
        }
        return null; // Member not found
    }

    public Member updateBio(Long memberId, String newBio) {
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member != null) {
            member.setBio(newBio);
            return memberRepository.save(member);
        }
        return null; // Member not found
    }

    public Member updateMemberProfile(String email, Member updatedMember) {
        if (email != null) {
            Member existingMember = memberRepository.findByEmail(email);
            if (existingMember != null) {
                // Update fields except enrollment
                existingMember.setFirstName(updatedMember.getFirstName());
                existingMember.setLastName(updatedMember.getLastName());
                existingMember.setEmail(updatedMember.getEmail());
                existingMember.setGender(updatedMember.getGender());
                existingMember.setNationality(updatedMember.getNationality());
                existingMember.setProfession(updatedMember.getProfession());
                existingMember.setOrganization(updatedMember.getOrganization());
                existingMember.setPosition(updatedMember.getPosition());
                existingMember.setExpertise(updatedMember.getExpertise());
                existingMember.setLanguage(updatedMember.getLanguage());
                existingMember.setBio(updatedMember.getBio());

                return memberRepository.save(existingMember);
            }
        }

        return null; // Member not found or not updated
    }

    public void deleteMemberByEmail(String email) {
        Member member = memberRepository.findByEmail(email);
        if (member != null) {
            // Delete the member from the tbl_member table
            memberRepository.delete(member);

            // You may also want to delete the corresponding user record from the tbl_user table
            User user = userRepository.findByEmail(email);
            if (user != null) {
                userRepository.delete(user);

                // Send an email to the user
                emailService.sendAccountDeletedEmail(email); // Implement this method in EmailService
            } else {
                throw new RuntimeException("User not found with email: " + email);
            }
        } else {
            throw new RuntimeException("Member not found with email: " + email);
        }
        
    }
    
    
    public void quitNetworkByEmail(String email) {
        // Find the member by their email
        Member member = memberRepository.findByEmail(email);

        if (member != null) {
            // Delete the member from the tbl_member table
            memberRepository.delete(member);

            // Delete the corresponding user record from the tbl_user table
            User user = userRepository.findByEmail(email);
            if (user != null) {
                userRepository.delete(user);

                // Send an email to the user
                emailService.sendAccountDeletedEmail(email); // Implement this method in EmailService
            } else {
                throw new RuntimeException("User not found with email: " + email);
            }
        } else {
            throw new RuntimeException("Member not found with email: " + email);
        }
    }
    
    public List<Member> findByNationality(String nationality) {
        return memberRepository.findByNationality(nationality);
    }

    
    public List<Member> findByExpertise(String expertise) {
        return memberRepository.findByExpertise(expertise);
    }
    
    public int getTotalMembersCount() {
        // Retrieve the total count of members from the repository
        return memberRepository.countAllMembers(); 
    }

    public List<Member> getMembersByPage(int page, int pageSize) {
        // Calculate the offset based on the page number and page size
        int offset = (page - 1) * pageSize;

        // Retrieve members for the specified page
        return memberRepository.findMembersByPage(offset, pageSize); 
    }
    
    public List<Member> searchMembers(String keyword) {
      
        List<Member> foundMembers = memberRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(keyword, keyword);

        return foundMembers;
    }


    
    public void updateProfile(Member member, MultipartFile photo) throws IOException {
        // Handle profile picture update if a new photo is provided
        if (photo != null && !photo.isEmpty()) {
            member.setPhoto(photo.getBytes());
            member.setBase64Photo(Base64.getEncoder().encodeToString(photo.getBytes()));
        }

        // Update member's profile with the provided data
        memberRepository.save(member);
    }

    
    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }
}
