package net.pheocnetafr.africapheocnet.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import net.pheocnetafr.africapheocnet.entity.Member;
import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.forum.ForumTopicRepository;
import net.pheocnetafr.africapheocnet.repository.MemberRepository;
import net.pheocnetafr.africapheocnet.repository.ProjectRepository;
import net.pheocnetafr.africapheocnet.repository.TrainerRepository;
import net.pheocnetafr.africapheocnet.repository.UserRepository;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

@Controller
public class DashboardController {
	
	@Autowired
	private TrainerRepository trainerRepository; 
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ForumTopicRepository forumTopicRepository;
	
	@Autowired
	private ProjectRepository projectRepository;
	
	@Autowired
	private MemberRepository memberRepository;
	 
	@GetMapping("/admin_dashboard")
	public String adminDashboard(Principal principal, Model model) {
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
		model.addAttribute("pageTitle", "Admin Dashboard | Africa PHEOC-Net");

		long totalUsers = userRepository.count();
		long totalTrainedTrainers = trainerRepository.count();
		long totalDiscussions = forumTopicRepository.count();

		model.addAttribute("totalUsers", totalUsers);
		model.addAttribute("totalTrainedTrainers", totalTrainedTrainers);
		model.addAttribute("totalDiscussions", totalDiscussions);

		// Fetch total number of ongoing and completed projects
		long ongoingProjects = projectRepository.countByStatus("Ongoing");
		long completedProjects = projectRepository.countByStatus("Completed");
		model.addAttribute("ongoingProjects", ongoingProjects);
		model.addAttribute("completedProjects", completedProjects);

		// Fetch country-wise member count
		List<Object[]> countryMemberCount = memberRepository.countMembersByNationality();
		model.addAttribute("countryMemberCount", countryMemberCount);

		// Fetch profession distribution
		List<Object[]> professionDistribution = memberRepository.countMembersByProfession();
		model.addAttribute("professionDistribution", professionDistribution);

		// Fetch organization distribution
		List<Object[]> organizationDistribution = memberRepository.countMembersByOrganization();
		model.addAttribute("organizationDistribution", organizationDistribution);

		// Fetch enrollment data for the past three months
		LocalDate endDate = LocalDate.now();
		LocalDate startDate = endDate.minusMonths(3);
		List<Object[]> enrollmentData = memberRepository.getEnrollmentData(startDate, endDate);
		model.addAttribute("enrollmentData", enrollmentData);

		return "admin_dashboard";
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

	@GetMapping("/trainer_dashboard")
	public String trainerDashboard(Principal principal, Model model) {
		// You can access the authenticated user's information using the Principal object
		model.addAttribute("username", principal.getName());
		return "trainer_dashboard";
	}

	@GetMapping("/user_dashboard")
	public String userDashboard(Principal principal, Model model) {
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

		model.addAttribute("pageTitle", "User Dashboard | Africa PHEOC-Net");

		long totalUsers = userRepository.count();
		long totalTrainedTrainers = trainerRepository.count();
		long totalDiscussions = forumTopicRepository.count();

		model.addAttribute("totalUsers", totalUsers);
		model.addAttribute("totalTrainedTrainers", totalTrainedTrainers);
		model.addAttribute("totalDiscussions", totalDiscussions);

		return "user_dashboard";
	}
}
