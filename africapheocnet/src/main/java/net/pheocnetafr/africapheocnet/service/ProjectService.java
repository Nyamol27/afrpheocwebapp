package net.pheocnetafr.africapheocnet.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.pheocnetafr.africapheocnet.entity.Project;
import net.pheocnetafr.africapheocnet.entity.WorkingGroup;
import net.pheocnetafr.africapheocnet.repository.ProjectRepository;
import net.pheocnetafr.africapheocnet.repository.WorkingGroupRepository;
import net.pheocnetafr.africapheocnet.util.WorkingGroupNotFoundException;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final WorkingGroupRepository workingGroupRepository;

    
    @Autowired
    public ProjectService(ProjectRepository projectRepository, WorkingGroupRepository workingGroupRepository) {
        this.projectRepository = projectRepository;
        this.workingGroupRepository = workingGroupRepository;
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Project createProject(Project project) {
        return projectRepository.save(project);
    }

    public Project getProjectById(Long id) {
        return projectRepository.findById(id).orElse(null);
    }

    public void updateProject(Long id, Project updatedProject) {
        Project existingProject = getProjectById(id);
        if (existingProject != null) {
            existingProject.setProjectName(updatedProject.getProjectName());
            existingProject.setWorkingGroup(updatedProject.getWorkingGroup());
            existingProject.setStartDate(updatedProject.getStartDate());
            existingProject.setEndDate(updatedProject.getEndDate());
            existingProject.setStatus(updatedProject.getStatus());
            existingProject.setInitiator(updatedProject.getInitiator());
            projectRepository.save(existingProject);
        }
    }

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    public List<Project> getProjectsByWorkingGroup(WorkingGroup workingGroup) {
        return projectRepository.findByWorkingGroup(workingGroup);
    }

    public List<Project> getProjectsByWorkingGroupName(String workingGroupName) {
        WorkingGroup workingGroup = workingGroupRepository.findByName(workingGroupName);
        if (workingGroup != null) {
            return projectRepository.findByWorkingGroup(workingGroup);
        } else {
            return Collections.emptyList(); 
        }
    }

    public int getCompletedProjectsCountByWorkingGroup(WorkingGroup workingGroup) {
        return projectRepository.countByWorkingGroupAndStatus(workingGroup, "Completed");
    }

    public int getOngoingProjectsCountByWorkingGroup(WorkingGroup workingGroup) {
        return projectRepository.countByWorkingGroupAndStatus(workingGroup, "Ongoing");
    }

    public int getDelayedProjectsCountByWorkingGroup(WorkingGroup workingGroup) {
        return projectRepository.countByWorkingGroupAndStatus(workingGroup, "Delayed");
    }

    public WorkingGroup getWorkingGroupByName(String name) {
        return workingGroupRepository.findByName(name);
    }
    
   
    public Long getWorkingGroupIdByProjectId(Long projectId) {
        Optional<Project> projectOptional = projectRepository.findById(projectId);

        if (projectOptional.isPresent()) {
            Project project = projectOptional.get();
            // Get the working group associated with the project
            WorkingGroup workingGroup = project.getWorkingGroup();
            // If working group is not null, return its ID
            if (workingGroup != null) {
                return workingGroup.getId();
            } else {
                // Handle the case where there is no associated working group
                // You might throw an exception or return null, depending on your requirements
                throw new WorkingGroupNotFoundException("No working group associated with project ID: " + projectId);
            }
        } else {
            // Handle the case where the project ID does not exist
            // You might throw an exception or return null, depending on your requirements
            throw new IllegalArgumentException("Project ID not found: " + projectId);
        }
    }
}
