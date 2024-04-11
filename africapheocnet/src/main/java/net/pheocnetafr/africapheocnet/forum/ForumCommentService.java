package net.pheocnetafr.africapheocnet.forum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ForumCommentService {

    private final ForumCommentRepository forumCommentRepository;

    @Autowired
    public ForumCommentService(ForumCommentRepository forumCommentRepository) {
        this.forumCommentRepository = forumCommentRepository;
    }

    public List<ForumComment> getAllComments() {
        return forumCommentRepository.findAll();
    }

    public Optional<ForumComment> getCommentById(Long id) {
        return forumCommentRepository.findById(id);
    }

    public ForumComment createComment(ForumComment comment) {
        return forumCommentRepository.save(comment);
    }

    public ForumComment updateComment(Long id, ForumComment updatedComment) {
        Optional<ForumComment> existingComment = forumCommentRepository.findById(id);

        if (existingComment.isPresent()) {
            ForumComment comment = existingComment.get();
            comment.setContent(updatedComment.getContent());
            comment.setAttachmentData(updatedComment.getAttachmentData());
            comment.setAttachmentContentType(updatedComment.getAttachmentContentType());

            return forumCommentRepository.save(comment);
        } else {
            return null;
        }
    }

    public boolean deleteComment(Long id) {
        if (forumCommentRepository.existsById(id)) {
            forumCommentRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    public List<ForumComment> getCommentsByTopicId(Long topicId) {
        return forumCommentRepository.findByTopicId(topicId);
    }

    public Long getCommentCountForTopic(Long topicId) {
        return forumCommentRepository.countByTopicId(topicId);
    }

    public Page<ForumComment> getCommentsForTopic(Long topicId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        return forumCommentRepository.findByTopicId(topicId, pageable);
    }
    
    public boolean deleteCommentById(Long commentId) {
        try {
            forumCommentRepository.deleteById(commentId);
            return true; 
        } catch (Exception e) {
            e.printStackTrace();
            return false; 
        }
    }
    
    public List<ForumComment> findCommentsByTopicId(Long topicId) {
        return forumCommentRepository.findByTopicId(topicId);
    }

}
