package net.pheocnetafr.africapheocnet.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tbl_working_group")
public class WorkingGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;
    
    @Column(name = "workingGroup")
    private String workingGroupName;
    
    private boolean joined;

 
    public WorkingGroup() {
    }

    // Getters and setters
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
    
    public String getWorkingGroupName() {
        return workingGroupName;
    }

    public void setWorkingGroupName(String workingGroupName) {
        this.workingGroupName = workingGroupName;
    }
    
    public boolean isJoined() {
        return joined;
    }

    public void setJoined(boolean joined) {
        this.joined = joined;
    }
    
   

    public static WorkingGroup createWorkingGroup(String name) {
        WorkingGroup workingGroup = new WorkingGroup();
        workingGroup.setName(name);
        return workingGroup;
    }
}
