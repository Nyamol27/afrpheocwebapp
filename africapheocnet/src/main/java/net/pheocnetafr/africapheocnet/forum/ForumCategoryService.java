package net.pheocnetafr.africapheocnet.forum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ForumCategoryService {
    private final ForumCategoryRepository forumCategoryRepository;

    @Autowired
    public ForumCategoryService(ForumCategoryRepository forumCategoryRepository) {
        this.forumCategoryRepository = forumCategoryRepository;
    }

    public Optional<ForumCategory> getCategoryById(Long id) {
        return forumCategoryRepository.findById(id);
    }

    public List<ForumCategory> getAllCategories() {
        return forumCategoryRepository.findAll();
    }

    public ForumCategory createCategory(ForumCategory category) {
        return forumCategoryRepository.save(category);
    }

    public ForumCategory updateCategory(Long id, ForumCategory updatedCategory) {
        Optional<ForumCategory> existingCategory = forumCategoryRepository.findById(id);

        if (existingCategory.isPresent()) {
            ForumCategory category = existingCategory.get();
            category.setName(updatedCategory.getName());
            // You can update other fields as needed
            return forumCategoryRepository.save(category);
        } else {
            // Handle the case where the category with the given ID is not found
            return null;
        }
    }

    public boolean deleteCategory(Long id) {
        Optional<ForumCategory> existingCategory = forumCategoryRepository.findById(id);

        if (existingCategory.isPresent()) {
            forumCategoryRepository.deleteById(id);
            return true;
        } else {
            // Handle the case where the category with the given ID is not found
            return false;
        }
    }
    
    public Optional<Long> findCategoryIdByName(String categoryName) {
        ForumCategory category = forumCategoryRepository.findByName(categoryName);
        if (category != null) {
            return Optional.of(category.getId());
        }
        return Optional.empty();
    }
}
