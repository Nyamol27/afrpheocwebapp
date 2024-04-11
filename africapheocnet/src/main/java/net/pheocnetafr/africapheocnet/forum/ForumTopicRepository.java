package net.pheocnetafr.africapheocnet.forum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumTopicRepository extends JpaRepository<ForumTopic, Long> {

    List<ForumTopic> findByCategory(ForumCategory category);

    List<ForumTopic> findByCategoryNameAndStatus(String categoryName, String status);

    List<ForumTopic> findByCategoryIdAndStatus(Long categoryId, String status);

    Long countByCategory(ForumCategory category);
    
    Long countByCategoryName(String categoryName);
    
    Long countByCategoryId(Long categoryId);
}
