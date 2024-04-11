package net.pheocnetafr.africapheocnet.forum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;
@RestController
@RequestMapping("/api/likes")
public class ForumLikeController {

    private final ForumLikeService forumLikeService;

    @Autowired
    private ForumLikeRepository likeRepository;

    @Autowired
    private ForumTopicRepository topicRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public ForumLikeController(ForumLikeService forumLikeService) {
        this.forumLikeService = forumLikeService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ForumLike> getLikeById(@PathVariable Long id) {
        Optional<ForumLike> likeOptional = forumLikeService.getLikeById(id);

        return likeOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<List<ForumLike>> getLikesByTopicId(@PathVariable Long topicId) {
        List<ForumLike> likes = forumLikeService.getLikesByTopicId(topicId);
        return ResponseEntity.ok(likes);
    }

    @PostMapping
    public ResponseEntity<String> createLike(@RequestBody ForumLike like) {
        ForumLike createdLike = forumLikeService.createLike(like);
        return ResponseEntity.ok("Like created successfully with ID: " + createdLike.getId());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteLike(@PathVariable Long id) {
        boolean deleted = forumLikeService.deleteLike(id);

        return deleted ? ResponseEntity.ok("Like deleted successfully") : ResponseEntity.notFound().build();
    }

    @GetMapping("/count/topic/{topicId}")
    public ResponseEntity<Long> getTopicLikeCount(@PathVariable Long topicId) {
        Long likeCount = forumLikeService.getLikeCountForTopic(topicId);
        return ResponseEntity.ok(likeCount);
    }

    @PostMapping("/like/{topicId}")
    public ResponseEntity<Map<String, Object>> likeTopic(@PathVariable Long topicId, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail);

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        ForumTopic topic = topicRepository.findById(topicId).orElse(null);

        if (topic == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        boolean alreadyLiked = likeRepository.existsByUserEmailAndTopicId(userEmail, topicId);

        if (alreadyLiked) {
            // If the user has already liked the topic, return a response indicating so
            Map<String, Object> response = new HashMap<>();
            response.put("message", "You have already liked this topic");
            return ResponseEntity.ok(response);
        }

        // If the user has not already liked the topic, proceed to save the like
        ForumLike like = new ForumLike();
        like.setUser(currentUser);
        like.setTopic(topic);
        likeRepository.save(like);

        // Increment the like count for the topic
        topic.setNumLikes(topic.getNumLikes() + 1);
        topicRepository.save(topic);

        // Prepare the response JSON
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Topic liked successfully");
        response.put("likes", topic.getNumLikes());

        return ResponseEntity.ok(response);
    }
}
