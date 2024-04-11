package net.pheocnetafr.africapheocnet.forum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForumCategoryRepository extends JpaRepository<ForumCategory, Long> {
	ForumCategory findByName(String name);
}
