package net.pheocnetafr.africapheocnet.service;

import net.pheocnetafr.africapheocnet.entity.WorkingGroup;
import net.pheocnetafr.africapheocnet.repository.WorkingGroupRepository;
import net.pheocnetafr.africapheocnet.util.WorkingGroupNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WorkingGroupService {
    private final WorkingGroupRepository workingGroupRepository;

    @Autowired
    public WorkingGroupService(WorkingGroupRepository workingGroupRepository) {
        this.workingGroupRepository = workingGroupRepository;
    }

    public List<WorkingGroup> getAllWorkingGroups() {
        return workingGroupRepository.findAll();
    }

    public Optional<WorkingGroup> getWorkingGroupById(Long id) {
        return workingGroupRepository.findById(id);
    }

    public WorkingGroup createWorkingGroup(WorkingGroup workingGroup) {
        return workingGroupRepository.save(workingGroup);
    }

    public WorkingGroup updateWorkingGroup(Long id, WorkingGroup updatedWorkingGroup) {
        Optional<WorkingGroup> optionalWorkingGroup = workingGroupRepository.findById(id);
        if (optionalWorkingGroup.isPresent()) {
            WorkingGroup existingWorkingGroup = optionalWorkingGroup.get();
            existingWorkingGroup.setName(updatedWorkingGroup.getName());
            return workingGroupRepository.save(existingWorkingGroup);
        }
        return null; 
    }

    public void deleteWorkingGroup(Long id) {
        workingGroupRepository.deleteById(id);
    }
    
    public Long getWorkingGroupIdByName(String groupName) {
        WorkingGroup workingGroup = workingGroupRepository.findByName(groupName);
        if (workingGroup != null) {
            return workingGroup.getId();
        } else {
            return null;
        }
    }
    

    public String getWorkingGroupNameById(Long workingGroupId) {
        // Find the WorkingGroup entity by ID
        Optional<WorkingGroup> optionalWorkingGroup = workingGroupRepository.findById(workingGroupId);

        // Check if the optional contains a value
        if (optionalWorkingGroup.isPresent()) {
            // Extract the WorkingGroup object from the Optional
            WorkingGroup workingGroup = optionalWorkingGroup.get();
            
            // Return the name of the working group
            return workingGroup.getName();
        } else {
            // Handle the case where no working group with the given ID exists
            throw new WorkingGroupNotFoundException("Working group with ID " + workingGroupId + " not found");
        }
    }
    
    public WorkingGroup findByWorkingGroupName(String workingGroupName) {
        return workingGroupRepository.findByWorkingGroupName(workingGroupName);
    }
    
    public WorkingGroup getWorkingGroupByName(String name) {
        return workingGroupRepository.findByName(name);
    }
}
