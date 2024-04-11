package net.pheocnetafr.africapheocnet.forum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ForumLikeService {
    private final ForumLikeRepository forumLikeRepository;

    @Autowired
    public ForumLikeService(ForumLikeRepository forumLikeRepository) {
        this.forumLikeRepository = forumLikeRepository;
    }

    public Optional<ForumLike> getLikeById(Long id) {
        return forumLikeRepository.findById(id);
    }

    public List<ForumLike> getLikesByTopicId(Long topicId) {
        return forumLikeRepository.findByTopicId(topicId);
    }

    public ForumLike createLike(ForumLike like) {
        return forumLikeRepository.save(like);
    }

    public boolean deleteLike(Long id) {
        Optional<ForumLike> existingLike = forumLikeRepository.findById(id);

        if (existingLike.isPresent()) {
            forumLikeRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }
    
    public Long getLikeCountByTopicId(Long topicId) {
        return forumLikeRepository.countLikesByTopicId(topicId);
    }
    public Long getLikeCountForComment(Long commentId) {
        return forumLikeRepository.countLikesByCommentId(commentId);
    }

    public Long getLikeCountForTopic(Long topicId) {
        return forumLikeRepository.countLikesByTopicId(topicId);
    }
    
   


}
