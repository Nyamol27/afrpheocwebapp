package net.pheocnetafr.africapheocnet.forum;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ForumLikeRepository extends JpaRepository<ForumLike, Long> {
    
    // Custom query to count likes by topic
    @Query("SELECT COUNT(fl) FROM ForumLike fl WHERE fl.topic.id = :topicId")
    Long countLikesByTopicId(@Param("topicId") Long topicId);
    
    // Custom query to count likes by comment
    @Query("SELECT COUNT(fl) FROM ForumLike fl WHERE fl.comment.id = :commentId")
    Long countLikesByCommentId(@Param("commentId") Long commentId);
    
    List<ForumLike> findByTopicId(Long topicId);
    
    int countByTopicId(Long topicId);
    
    boolean existsByUserEmailAndTopicId(String userEmail, Long topicId);
  
}
