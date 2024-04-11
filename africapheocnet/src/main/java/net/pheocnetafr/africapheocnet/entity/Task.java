package net.pheocnetafr.africapheocnet.entity;
import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "tbl_twgtask")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task", columnDefinition = "LONGTEXT")
    private String task;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "priority")
    private String priority;

    @Column(name = "status")
    private String status;

    @Column(name = "responsible")
    private String responsible;

    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne
    @JoinColumn(name = "working_group_id")
    private WorkingGroup workingGroup;

    // Additional variables for direct access to project_id and working_group_id
    @Transient
    private Long projectId;

    @Transient
    private Long workingGroupId;

    // Constructors
    public Task() {
        // Default constructor
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResponsible() {
        return responsible;
    }

    public void setResponsible(String responsible) {
        this.responsible = responsible;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public WorkingGroup getWorkingGroup() {
        return workingGroup;
    }

    public void setWorkingGroup(WorkingGroup workingGroup) {
        this.workingGroup = workingGroup;
    }

    public Long getProjectId() {
        if (project != null) {
            return project.getProjectId();
        }
        return null;
    }

    public void setProjectId(Long projectId) {
        
    	this.project = new Project();
    	this.project.setProjectId(projectId);
    }
    public Long getWorkingGroupId() {
        if (workingGroup != null) {
            return workingGroup.getId();
        }
        return null;
    }

    public void setWorkingGroupId(Long workingGroupId) {
    	this.workingGroup = new WorkingGroup();
        this.workingGroup.setId(workingGroupId);
    }
}
