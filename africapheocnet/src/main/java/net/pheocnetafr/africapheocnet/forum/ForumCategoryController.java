package net.pheocnetafr.africapheocnet.forum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import net.pheocnetafr.africapheocnet.entity.Member;
import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.repository.MemberRepository;
import net.pheocnetafr.africapheocnet.repository.UserRepository;
import net.pheocnetafr.africapheocnet.service.UserService;

import java.security.Principal;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/forum")
public class ForumCategoryController {
    private final ForumCategoryService forumCategoryService;
    
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ForumTopicService forumTopicService;
    
    @Autowired
    public ForumCategoryController(ForumCategoryService forumCategoryService) {
        this.forumCategoryService = forumCategoryService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ForumCategory> getCategoryById(@PathVariable Long id) {
        Optional<ForumCategory> category = forumCategoryService.getCategoryById(id);
        return category.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ForumCategory>> getAllCategories() {
        List<ForumCategory> categories = forumCategoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    public ResponseEntity<ForumCategory> createCategory(@RequestBody ForumCategory category) {
        ForumCategory createdCategory = forumCategoryService.createCategory(category);
        return ResponseEntity.ok(createdCategory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ForumCategory> updateCategory(@PathVariable Long id, @RequestBody ForumCategory updatedCategory) {
        ForumCategory category = forumCategoryService.updateCategory(id, updatedCategory);
        if (category != null) {
            return ResponseEntity.ok(category);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        boolean deleted = forumCategoryService.deleteCategory(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
 
    @GetMapping("/view/{category}")
    public String viewForum(@PathVariable String category, Model model, Principal principal) {
    	addUserInfoToModel(model, principal);
        // Get category ID by name
        Optional<Long> categoryIdOptional = forumCategoryService.findCategoryIdByName(category);

        if (categoryIdOptional.isPresent()) {
            Long categoryId = categoryIdOptional.get();
            System.out.println("Category ID found: " + categoryId);

            // Get topics by category ID and status
            List<ForumTopic> topics = forumTopicService.getTopicsByCategoryIdAndStatus(categoryId, "Published");

            // Print the list of topics
            System.out.println("List of topics:");
            for (ForumTopic topic : topics) {
                System.out.println(topic.getTitle()); // Print the title of each topic
                
                // Fetch user details based on user_id
                User user = topic.getUser();
                if (user != null) {
                    // Update the ForumTopic object with user's first name and last name
                    topic.setFirstName(user.getFirstName());
                    topic.setLastName(user.getLastName());
                }
            }

            // Calculate additional information for each topic
            for (ForumTopic topic : topics) {
                // Set the number of likes for each topic
                int numLikes = forumTopicService.getLikesForTopic(topic.getId());
                topic.setNumLikes(numLikes);

                // Set the total number of comments for each topic
                int numComments = forumTopicService.getCommentsForTopic(topic.getId());
                topic.setNumComments(numComments);

                // Set the last comment date as the last updated date
                Date lastCommentDate = forumTopicService.getLastCommentDateForTopic(topic.getId());
                if (lastCommentDate != null) {
                    topic.setLastUpdated(lastCommentDate);
                }
            }

            model.addAttribute("topics", topics);
            model.addAttribute("category", category);
            return "forum-view";
        } else {
            System.out.println("Category ID not found for category: " + category);
            // Handle the case when category ID is not found
            return "category-not-found";
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
