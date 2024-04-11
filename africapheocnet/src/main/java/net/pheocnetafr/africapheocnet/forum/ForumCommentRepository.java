package net.pheocnetafr.africapheocnet.forum;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Repository
public interface ForumCommentRepository extends JpaRepository<ForumComment, Long> {
    
    List<ForumComment> findByTopicId(Long topicId);
    

    Long countByTopicId(Long topicId);
    
    Page<ForumComment> findByTopicId(Long topicId, Pageable pageable);
    
   
    @Query(value = "SELECT MAX(created_at) FROM forum_comment WHERE topic_id = ?1", nativeQuery = true)
    Timestamp findLastCommentDateByTopicId(Long topicId);
  
}

