package net.pheocnetafr.africapheocnet.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import net.pheocnetafr.africapheocnet.entity.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
   
    int countByProject_ProjectIdAndStatus(Long projectId, String status);

    
    @Query("SELECT t FROM Task t WHERE t.project.projectId = :projectId")
    List<Task> findByProject_ProjectId(Long projectId);
    
   
    
    long countByWorkingGroup_Id(Long workingGroupId);

}
