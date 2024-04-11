package net.pheocnetafr.africapheocnet.forum;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "forum_category")
public class ForumCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ForumTopic> topics = new ArrayList<>();

	public List<ForumTopic> getTopics() {
		return topics;
	}

	public void setTopics(List<ForumTopic> topics) {
		this.topics = topics;
	}
	
    public ForumCategory(Long id) {
        this.id = id;
    }

    public ForumCategory() {
    }
}

