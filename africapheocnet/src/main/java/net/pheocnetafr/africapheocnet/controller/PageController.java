package net.pheocnetafr.africapheocnet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import net.pheocnetafr.africapheocnet.entity.Member;
import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.repository.MemberRepository;
import net.pheocnetafr.africapheocnet.repository.UserRepository;

import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.Principal;
import java.util.Base64;

@Controller
public class PageController {

	@Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("/home")
    public String showIndex(Model model, Principal principal) {
    	model.addAttribute("pageTitle", "Home | Africa PHEOC-Net");
        return "index";
    }
    
    
    @GetMapping("/about")
    public String showAbout(Model model) {
        model.addAttribute("pageTitle", "About Us | Africa PHEOC-Net");
        return "about";
    }

    @GetMapping("/membership")
    public String showMembership(Model model) {
        model.addAttribute("pageTitle", "Membership | Africa PHEOC-Net");
        return "membership";
    }

    @GetMapping("/events")
    public String showEvents(Model model) {
        model.addAttribute("pageTitle", "Events | Africa PHEOC-Net");
        return "events";
    }

    @GetMapping("/partnership")
    public String showPartnership(Model model) {
        model.addAttribute("pageTitle", "Partnership | Africa PHEOC-Net");
        return "partnership";
    }

    @GetMapping("/advocacy")
    public String showAdvocacy(Model model) {
        model.addAttribute("pageTitle", "Advocacy | Africa PHEOC-Net");
        return "advocacy";
    }

    @GetMapping("/country-materials")
    public String showCountryMaterials(Model model) {
        model.addAttribute("pageTitle", "Country Specific Materials | Africa PHEOC-Net");
        return "country-materials";
    }

    @GetMapping("/publications")
    public String showPublications(Model model) {
        model.addAttribute("pageTitle", "Publications | Africa PHEOC-Net");
        return "publications";
    }

    @GetMapping("/scientific-papers")
    public String showScientificPapers(Model model) {
        model.addAttribute("pageTitle", "Scientific Papers | Africa PHEOC-Net");
        return "scientific-papers";
    }

    @GetMapping("/standard-training")
    public String showStandardTraining(Model model) {
        model.addAttribute("pageTitle", "Standard Training Materials | Africa PHEOC-Net");
        return "standard-training";
    }

    @GetMapping("/contact")
    public String showContact(Model model) {
        model.addAttribute("pageTitle", "Contact Us | Africa PHEOC-Net");
        return "contact";
    }
    
    @GetMapping("/faq")
    public String showFAQ(Model model) {
        model.addAttribute("pageTitle", "Q&A | Africa PHEOC-Net");
        return "faq";
    }
    
    @GetMapping("/resetpassword")
    public String showResetPassword(Model model) {
        model.addAttribute("pageTitle", "Reset Password | Africa PHEOC-Net");
        return "resetpassword";
    }

    @GetMapping("/setpassword")
    public String showSetPassword(Model model) {
        model.addAttribute("pageTitle", "Reset Password | Africa PHEOC-Net");
        return "setpassword";
    }

    
    @GetMapping("/message")
    public String showMessage(Model model) {
        model.addAttribute("pageTitle", " Message | Africa PHEOC-Net");
        return "message";
    }
    
    
    @GetMapping("/success-page")
    public String showSuccessPage(Model model) {
        model.addAttribute("pageTitle", "Success Page | Africa PHEOC-Net");
        return "success-page";
    }

    @GetMapping("/apps-forum")
    public String showAppsForum(Model model, Principal principal) {
        addUserInfoToModel(model, principal);
        model.addAttribute("pageTitle", "Apps Forum | Africa PHEOC-Net");
        return "apps-forum";
    }

    @GetMapping("/apps-working-group")
    public String showAppsWorkingGroup(Model model, Principal principal) {
        addUserInfoToModel(model, principal);
        model.addAttribute("pageTitle", "Apps Working Group | Africa PHEOC-Net");
        return "apps-working-group";
    }

    @GetMapping("/apps-invite")
    public String showAppsInvite(Model model, Principal principal) {
        addUserInfoToModel(model, principal);
        model.addAttribute("pageTitle", "Invite a colleague | Africa PHEOC-Net");
        return "apps-invite";
    }

    @GetMapping("trainer/apps-tot-club")
    public String showAppsTotClub(Model model, Principal principal) {
        addUserInfoToModel(model, principal);
        model.addAttribute("pageTitle", "ToT Club | Africa PHEOC-Net");
        return "apps-tot-club";
    }

    @GetMapping("admin/apps-administration")
    public String showAppsAdministration(Model model, Principal principal) {
        addUserInfoToModel(model, principal);
        model.addAttribute("pageTitle", "Management Dashboard | Africa PHEOC-Net");
        return "apps-administration";
    }

    @GetMapping("/apps-faq")
    public String showAppsFaq(Model model, Principal principal) {
        addUserInfoToModel(model, principal);
        model.addAttribute("pageTitle", " FAQ | Africa PHEOC-Net");
        return "apps-faq";
    }

    @GetMapping("/apps-feedback")
    public String showAppsFeedback(Model model, Principal principal) {
        addUserInfoToModel(model, principal);
        model.addAttribute("pageTitle", "Feedback | Africa PHEOC-Net");
        return "apps-feedback";
    }

    @GetMapping("/apps-twg")
    public String showAppsTwg(Model model, Principal principal) {
        addUserInfoToModel(model, principal);
        model.addAttribute("pageTitle", " TWG | Africa PHEOC-Net");
        return "apps-twg";
    }

    @GetMapping("/apps-comments")
    public String showAppsComments(Model model, Principal principal) {
        addUserInfoToModel(model, principal);
        model.addAttribute("pageTitle", " Comments | Africa PHEOC-Net");
        return "apps-comments-list";
    }

    @GetMapping("/application-form")
    public String showApplicationForm(Model model) {
       
        model.addAttribute("pageTitle", " Join Us | Africa PHEOC-Net");
        return "application-form";
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
