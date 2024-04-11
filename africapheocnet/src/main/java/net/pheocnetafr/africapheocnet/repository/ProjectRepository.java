package net.pheocnetafr.africapheocnet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.pheocnetafr.africapheocnet.entity.Project;
import net.pheocnetafr.africapheocnet.entity.WorkingGroup;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByWorkingGroup(WorkingGroup workingGroup);
    
    
    // Method to count projects by working group and status
    int countByWorkingGroupAndStatus(WorkingGroup workingGroup, String status);
    
    int countCompletedProjectsByWorkingGroup(WorkingGroup workingGroup);

    // Method to count ongoing projects by working group
    int countOngoingProjectsByWorkingGroup(WorkingGroup workingGroup);

    // Method to count delayed projects by working group
    int countDelayedProjectsByWorkingGroup(WorkingGroup workingGroup);
    
    // Method to count projects by status
    long countByStatus(String status);
}

