package net.pheocnetafr.africapheocnet.repository;

import net.pheocnetafr.africapheocnet.entity.WorkingGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkingGroupRepository extends JpaRepository<WorkingGroup, Long> {
    WorkingGroup findByName(String name);
    WorkingGroup findByWorkingGroupName(String workingGroupName);
}
