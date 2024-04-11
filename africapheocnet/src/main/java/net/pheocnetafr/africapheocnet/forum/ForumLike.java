package net.pheocnetafr.africapheocnet.forum;

import jakarta.persistence.*;
import net.pheocnetafr.africapheocnet.entity.User;

@Entity
@Table(name = "forum_like")
public class ForumLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "topic_id")
    private ForumTopic topic;

    @ManyToOne
    @JoinColumn(name = "comment_id")
    private ForumComment comment;

    @ManyToOne
    @JoinColumn(name = "user_email")
    private User user;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ForumTopic getTopic() {
		return topic;
	}

	public void setTopic(ForumTopic topic) {
		this.topic = topic;
	}

	public ForumComment getComment() {
		return comment;
	}

	public void setComment(ForumComment comment) {
		this.comment = comment;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	// Method to increment likes for the associated forum topic
    public void incrementTopicLikes() {
        if (this.topic != null) {
            this.topic.setNumLikes(this.topic.getNumLikes() + 1);
        }
    }
}

