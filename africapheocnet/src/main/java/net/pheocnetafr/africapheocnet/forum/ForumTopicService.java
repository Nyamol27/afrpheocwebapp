package net.pheocnetafr.africapheocnet.forum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.repository.UserRepository;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.sql.rowset.serial.SerialBlob;

@Service
public class ForumTopicService {
	
	  @Autowired
	   private ForumTopicRepository topicRepository;
	   private final ForumTopicRepository forumTopicRepository;
	    private final ForumCategoryRepository forumCategoryRepository;
	    private final UserRepository userRepository;
	    private final ForumLikeRepository forumLikeRepository;
	    private final ForumCommentRepository forumCommentRepository; // Added

	    @Autowired
	    public ForumTopicService(ForumTopicRepository forumTopicRepository,
	                             ForumCategoryRepository forumCategoryRepository,
	                             UserRepository userRepository,
	                             ForumLikeRepository forumLikeRepository,
	                             ForumCommentRepository forumCommentRepository) {
	        this.forumTopicRepository = forumTopicRepository;
	        this.forumCategoryRepository = forumCategoryRepository;
	        this.userRepository = userRepository;
	        this.forumLikeRepository = forumLikeRepository;
	        this.forumCommentRepository = forumCommentRepository; 
	    }

    @Transactional
    public ForumTopic createTopic(ForumTopic topic, Long categoryId, String userEmail, byte[] attachment) {
        Optional<ForumCategory> categoryOptional = forumCategoryRepository.findById(categoryId);
        User user = userRepository.findByEmail(userEmail);

        if (categoryOptional.isPresent() && user != null) {
            ForumCategory category = categoryOptional.get();

            topic.setCategory(category);
            topic.setUser(user);

            // Set the attachment if provided
            if (attachment != null && attachment.length > 0) {
                try {
                    Blob attachmentBlob = new SerialBlob(attachment);
                    topic.setAttachment(attachmentBlob);
                } catch (SQLException e) {
                   
                    e.printStackTrace();
                    
                }
            }

            return forumTopicRepository.save(topic);
        } else {
           
            throw new IllegalArgumentException("Category or user not found");
        }
    }

    public List<ForumTopic> getAllTopics() {
        return forumTopicRepository.findAll();
    }

    public List<ForumTopic> getTopicsByCategory(ForumCategory category) {
        return forumTopicRepository.findByCategory(category);
    }

    public Optional<ForumTopic> getTopicById(Long topicId) {
        return forumTopicRepository.findById(topicId);
    }

    public void deleteTopicById(Long topicId) {
        forumTopicRepository.deleteById(topicId);
    }
    
    public Long countTopicsByCategory(ForumCategory category) {
        return forumTopicRepository.countByCategory(category);
    }

   
    public List<ForumTopic> getTopicsByCategory(String category) {
        return forumTopicRepository.findByCategoryNameAndStatus(category, "Published");
    }
    
    public List<ForumTopic> getTopicsByCategoryAndStatus(String category, String status) {
        return forumTopicRepository.findByCategoryNameAndStatus(category, status);
    }

    
    public int getCommentsForTopic(Long topicId) {
        List<ForumComment> comments = forumCommentRepository.findByTopicId(topicId);
        return comments.size();
    }
    
    public int getLikesForTopic(Long topicId) {
        return forumLikeRepository.countByTopicId(topicId);
    }
    
    public Date getLastCommentDateForTopic(Long topicId) {
        return forumCommentRepository.findLastCommentDateByTopicId(topicId);
    }
    
    public List<ForumTopic> getTopicsByCategoryIdAndStatus(Long categoryId, String status) {
        return forumTopicRepository.findByCategoryIdAndStatus(categoryId, status);
    }

    public ForumTopic saveTopic(ForumTopic topic) {
        return forumTopicRepository.save(topic);
    }
    
    public ForumTopic findTopicById(Long id) {
        Optional<ForumTopic> topicOptional = forumTopicRepository.findById(id);
        return topicOptional.orElse(null);
    }
    
    public ForumTopic findById(Long id) {
        return forumTopicRepository.findById(id).orElse(null);
    }
    
    public void deleteTopic(Long topicId) {
       
        topicRepository.deleteById(topicId);
    }
    public Long getTopicCountByCategory(String categoryName) {
        
        return topicRepository.countByCategoryName(categoryName);
    }
    public Long countTopicsByCategoryName(String categoryName) {
        return topicRepository.countByCategoryName(categoryName);
    }
    
    public Optional<Long> countTopicsByCategoryId(Long categoryId) {
        Long count = topicRepository.countByCategoryId(categoryId);
        return Optional.ofNullable(count);
    }

    public ForumTopic getById(Long id) {
        return forumTopicRepository.findById(id).orElse(null);
    }

}
