package net.pheocnetafr.africapheocnet.controller;
import java.util.Optional;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import net.pheocnetafr.africapheocnet.entity.Member;
import net.pheocnetafr.africapheocnet.entity.Project;
import net.pheocnetafr.africapheocnet.entity.Task;
import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.entity.WorkingGroup;
import net.pheocnetafr.africapheocnet.repository.MemberRepository;
import net.pheocnetafr.africapheocnet.repository.ProjectRepository;
import net.pheocnetafr.africapheocnet.repository.UserRepository;
import net.pheocnetafr.africapheocnet.service.ProjectService;
import net.pheocnetafr.africapheocnet.service.TaskService;
import net.pheocnetafr.africapheocnet.util.WorkingGroupNotFoundException;

import org.springframework.validation.annotation.Validated;
import org.springframework.validation.BindingResult;



@Controller
@RequestMapping("/tasks")
public class TaskController {

	private final TaskService taskService;
    private final ProjectService projectService;
    
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public TaskController(TaskService taskService, ProjectService projectService) {
        this.taskService = taskService;
        this.projectService = projectService;
    }

    @GetMapping
    public String getAllTasks(Model model) {
        model.addAttribute("tasks", taskService.getAllTasks());
        return "task/list"; 
    }

    @GetMapping("/create")
    public String createTaskForm(Model model,Principal principal) {
    	addUserInfoToModel(model, principal);
        model.addAttribute("task", new Task());
        return "task/create"; 
    }

    @PostMapping("/create")
    public ResponseEntity<String> createTask(@RequestBody Task taskRequest) {
        try {
            // Retrieve the project ID and working group ID from the request
            Long projectId = taskRequest.getProjectId();
            Long workingGroupId = taskRequest.getWorkingGroupId();


            Task newTask = new Task();
            newTask.setTask(taskRequest.getTask());
            newTask.setPriority(taskRequest.getPriority());
            newTask.setResponsible(taskRequest.getResponsible());
            newTask.setStartDate(taskRequest.getStartDate());
            newTask.setTargetDate(taskRequest.getTargetDate());
            newTask.setStatus(taskRequest.getStatus());
            newTask.setProjectId(projectId); 
            newTask.setWorkingGroupId(workingGroupId); 

            // Save the new task
            taskService.createTask(newTask);

            return ResponseEntity.ok("Task created successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create task: " + e.getMessage());
        }
    }


    @GetMapping("/{id}")
    public String getTaskById(@PathVariable Long id, Model model) {
        Task task = taskService.getTaskById(id);
        if (task == null) {
            return "error/404"; 
        }
        model.addAttribute("task", task);
        return "task/details"; 
    }

    @GetMapping("/edit/{id}")
    public String editTaskForm(@PathVariable Long id, Model model,Principal principal) {
    	addUserInfoToModel(model, principal);
        Task task = taskService.getTaskById(id);
        if (task == null) {
            return "error/404"; 
        }
        model.addAttribute("task", task);
        return "task-edit"; 
    }

    @PostMapping("/edit/{id}")
    public String editTask(@PathVariable Long id, @Validated @ModelAttribute("task") Task task, BindingResult result) {
        if (result.hasErrors()) {
            return "task/edit"; 
        }
        taskService.updateTask(id, task);
        return "redirect:/tasks"; 
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable Long id) {
        try {
            taskService.deleteTaskById(id);
            return new ResponseEntity<>("Task with ID " + id + " deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete task with ID " + id + ". Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    
    @GetMapping("/view/{projectId}")
    public String viewTasksForProject(@PathVariable Long projectId, Model model, Principal principal) {
    	addUserInfoToModel(model, principal);
        List<Task> tasks = taskService.getTasksByProjectId(projectId);
        model.addAttribute("tasks", tasks);
        model.addAttribute("projectId", projectId);
        return "view-tasks"; 
    }
    
    @PostMapping("/update/{id}")
    public ResponseEntity<String> updateTask(@PathVariable Long id, @RequestBody Task updatedTask) {
        try {
            Task taskToUpdate = taskService.findById(id);
            if (taskToUpdate == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Task with ID " + id + " not found.");
            }

            // Update task details
            taskToUpdate.setTask(updatedTask.getTask());
            taskToUpdate.setStartDate(updatedTask.getStartDate());
            taskToUpdate.setTargetDate(updatedTask.getTargetDate());
            taskToUpdate.setPriority(updatedTask.getPriority());
            taskToUpdate.setResponsible(updatedTask.getResponsible());
            taskToUpdate.setStatus(updatedTask.getStatus());

            taskService.save(taskToUpdate); 
            return ResponseEntity.ok("Task updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating task: " + e.getMessage());
        }
    }
    
    private void addUserInfoToModel(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            User user = userRepository.findByEmail(username);
            if (user != null) {
                String firstName = user.getFirstName();
                String lastName = user.getLastName();
                String initials = firstName.substring(0, 1) + lastName.substring(0, 1);
                model.addAttribute("firstName", firstName);
                model.addAttribute("lastName", lastName);
                model.addAttribute("initials", initials);
            }
            // Fetch member information including the photo
            Member member = memberRepository.findByEmail(username);
            if (member != null) {
                convertPhotoToBase64(member);
                model.addAttribute("member", member);
            }
        }
    }

    private void convertPhotoToBase64(Member member) {
        if (member != null && member.getPhoto() != null) {
            // Convert binary photo data to Base64-encoded string
            byte[] photoBytes = member.getPhoto();
            String base64Photo = Base64.getEncoder().encodeToString(photoBytes);
            // Set the Base64-encoded photo string to the member object
            member.setBase64Photo(base64Photo);
        }
    }
}
