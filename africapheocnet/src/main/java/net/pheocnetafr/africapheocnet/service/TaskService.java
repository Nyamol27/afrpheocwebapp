package net.pheocnetafr.africapheocnet.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import net.pheocnetafr.africapheocnet.entity.Task;
import net.pheocnetafr.africapheocnet.repository.TaskRepository;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    public void updateTask(Long id, Task updatedTask) {
        Task existingTask = getTaskById(id);
        if (existingTask != null) {
            existingTask.setTask(updatedTask.getTask());
            existingTask.setStartDate(updatedTask.getStartDate());
            existingTask.setTargetDate(updatedTask.getTargetDate());
            existingTask.setPriority(updatedTask.getPriority());
            existingTask.setStatus(updatedTask.getStatus());
            existingTask.setResponsible(updatedTask.getResponsible());
            existingTask.setDescription(updatedTask.getDescription());
            
            existingTask.setProject(updatedTask.getProject());
            existingTask.setWorkingGroup(updatedTask.getWorkingGroup());
            
            taskRepository.save(existingTask);
        }
    }


    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
    
    public int getOngoingTasksCount(Long projectId) {
        return taskRepository.countByProject_ProjectIdAndStatus(projectId, "Ongoing");
    }

    public List<Task> getTasksByProjectId(Long projectId) {
        return taskRepository.findByProject_ProjectId(projectId);
    }
    
    public long getTasksCountByWorkingGroup(Long workingGroupId) {
      
        long tasksCount = taskRepository.countByWorkingGroup_Id(workingGroupId);
        return tasksCount;
    }
    
    public Task findById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }
    
    public Task save(Task task) {
        return taskRepository.save(task);
    }
    
    @Transactional
    public void deleteTaskById(Long id) throws Exception {
        // Find the task by ID
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new Exception("Task not found with ID: " + id));

        // Delete the task
        taskRepository.delete(task);
    }
}

