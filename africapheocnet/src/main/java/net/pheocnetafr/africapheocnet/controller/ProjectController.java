package net.pheocnetafr.africapheocnet.controller;

import net.pheocnetafr.africapheocnet.entity.Member;
import net.pheocnetafr.africapheocnet.entity.Project;
import net.pheocnetafr.africapheocnet.entity.Task;
import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.entity.WorkingGroup;
import net.pheocnetafr.africapheocnet.repository.MemberRepository;
import net.pheocnetafr.africapheocnet.repository.UserRepository;
import net.pheocnetafr.africapheocnet.service.ProjectService;
import net.pheocnetafr.africapheocnet.service.WorkingGroupService;
import net.pheocnetafr.africapheocnet.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final WorkingGroupService workingGroupService;
    private final TaskService taskService;
    
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public ProjectController(ProjectService projectService, WorkingGroupService workingGroupService, TaskService taskService) {
        this.projectService = projectService;
        this.workingGroupService = workingGroupService;
        this.taskService = taskService;
    }

    // Mapping to list all projects
    @GetMapping("/list")
    public String getAllProjects(Model model,Principal principal) {
    	addUserInfoToModel(model, principal);
        List<Project> projects = projectService.getAllProjects();
        model.addAttribute("projects", projects);
        return "project-list"; 
    }

    // Mapping to get a project by ID
    @GetMapping("view/{id}")
    public String getProjectById(@PathVariable Long id, Model model,Principal principal) {
    	addUserInfoToModel(model, principal);
        Project project = projectService.getProjectById(id);
        if (project == null) {
            throw new IllegalArgumentException("Invalid project ID: " + id);
        }
        model.addAttribute("project", project);
        return "project-details"; 
    }

    // Mapping to show form for creating a new project
    @GetMapping("/create")
    public String createProjectForm(Model model,Principal principal) {
    	addUserInfoToModel(model, principal);
        model.addAttribute("project", new Project());
        return "project/create"; // Return the view for creating a new project
    }

    @PostMapping("/add")
    public ResponseEntity<String> addProject(@RequestBody Project newProject, @RequestParam String workingGroupName) {
        try {
            // Retrieve the working group based on the provided name
            WorkingGroup workingGroup = workingGroupService.getWorkingGroupByName(workingGroupName);
            if (workingGroup == null) {
                return ResponseEntity.badRequest().body("Working group not found: " + workingGroupName);
            }

            // Set the retrieved working group for the new project
            newProject.setWorkingGroup(workingGroup);

            // Validate the new project details
            if (newProject.getProjectName() == null ||
                newProject.getStartDate() == null ||
                newProject.getEndDate() == null) {
                return ResponseEntity.badRequest().body("Invalid project details");
            }

            // Check if start date is not later than end date
            if (newProject.getStartDate().after(newProject.getEndDate())) {
                return ResponseEntity.badRequest().body("Start date cannot be later than end date");
            }

            // Add the new project
            projectService.createProject(newProject);

            return ResponseEntity.ok("Project added successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding project: " + e.getMessage());
        }
    }




    @PostMapping("/update/{id}")
    public ResponseEntity<String> updateProject(@PathVariable Long id, @RequestBody Project updatedProject) {
        try {
            Project projectToUpdate = projectService.getProjectById(id);
            if (projectToUpdate == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Project with ID " + id + " not found.");
            }

            
            projectToUpdate.setProjectName(updatedProject.getProjectName());
            projectToUpdate.setStartDate(updatedProject.getStartDate());
            projectToUpdate.setEndDate(updatedProject.getEndDate());
            projectToUpdate.setStatus(updatedProject.getStatus());

            projectService.updateProject(id, projectToUpdate); 

            return ResponseEntity.ok("Project updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating project: " + e.getMessage());
        }
    }



    // Mapping to handle deletion of a project
    @PostMapping("/delete/{id}")
    public ResponseEntity<String> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok("Project deleted successfully");
    }

    // Mapping to get projects by working group ID
    @GetMapping("/by-working-group/{workingGroupId}")
    public String getProjectsByWorkingGroup(@PathVariable Long workingGroupId, Model model, Principal principal) {
    	addUserInfoToModel(model, principal);
        WorkingGroup workingGroup = workingGroupService.getWorkingGroupById(workingGroupId).orElse(null);
        if (workingGroup != null) {
            List<Project> projects = projectService.getProjectsByWorkingGroup(workingGroup);
            model.addAttribute("projects", projects);
            model.addAttribute("workingGroup", workingGroup);
            model.addAttribute("ongoingTasksCountMap", getOngoingTasksCountMap(projects));
            return "projects-by-working-group";
        } else {
            throw new IllegalArgumentException("Working group not found with ID: " + workingGroupId);
        }
    }

 // Helper method to get ongoing tasks count map
    private Map<Long, Integer> getOngoingTasksCountMap(List<Project> projects) {
        Map<Long, Integer> ongoingTasksCountMap = new HashMap<>();
        for (Project project : projects) {
            int ongoingTasksCount = taskService.getOngoingTasksCount(project.getProjectId());
            ongoingTasksCountMap.put(project.getProjectId(), ongoingTasksCount);
        }
        return ongoingTasksCountMap;
    }


    // Mapping to view projects by working group name
    @GetMapping("/working-group/view/{workingGroupName}")
    public String viewProjectsByWorkingGroup(@PathVariable String workingGroupName, Model model,Principal principal) {
    	addUserInfoToModel(model, principal);
        WorkingGroup workingGroup = workingGroupService.getWorkingGroupByName(workingGroupName);
        if (workingGroup != null) {
            List<Project> projects = projectService.getProjectsByWorkingGroup(workingGroup);
            int completedProjectsCount = projectService.getCompletedProjectsCountByWorkingGroup(workingGroup);
            int ongoingProjectsCount = projectService.getOngoingProjectsCountByWorkingGroup(workingGroup);
            int delayedProjectsCount = projectService.getDelayedProjectsCountByWorkingGroup(workingGroup);
            
            // Calculate total projects
            int totalProjectsCount = completedProjectsCount + ongoingProjectsCount + delayedProjectsCount;

            model.addAttribute("projects", projects);
            model.addAttribute("workingGroup", workingGroup);
            model.addAttribute("pageTitle", "Projects for Working Group: " + workingGroupName);
            model.addAttribute("completedProjectsCount", completedProjectsCount);
            model.addAttribute("ongoingProjectsCount", ongoingProjectsCount);
            model.addAttribute("delayedProjectsCount", delayedProjectsCount);
            model.addAttribute("totalProjectsCount", totalProjectsCount); 
            
            return "view-projects";
        } else {
            model.addAttribute("message", "Working group not found: " + workingGroupName);
            return "error"; 
        }
    }
    
    // Mapping to list projects by working group
    @GetMapping("/list/{twg}")
    public String getProjectsByWorkingGroup(@PathVariable("twg") String workingGroupName, Model model,Principal principal) {
    	addUserInfoToModel(model, principal);
        // Retrieve the working group by name
        WorkingGroup workingGroup = workingGroupService.getWorkingGroupByName(workingGroupName);
        if (workingGroup != null) {
            // Get projects associated with the working group
            List<Project> projects = projectService.getProjectsByWorkingGroup(workingGroup);
            
            // Get the number of tasks pertaining to the working group
            long tasksCount = taskService.getTasksCountByWorkingGroup(workingGroup.getId());
            
            model.addAttribute("projects", projects);
            model.addAttribute("workingGroup", workingGroup);
            model.addAttribute("tasksCount", tasksCount);
            model.addAttribute("pageTitle", "TWG - Projects List | Africa PHEOC-Net");
            
            return "projects-list"; 
        } else {
            model.addAttribute("errorMessage", "Working group not found: " + workingGroupName);
            return "error"; 
        }
    }
    
    @GetMapping("/details/{projectId}")
    public String getProjectDetails(@PathVariable Long projectId, Model model,Principal principal) {
    	addUserInfoToModel(model, principal);
        Project project = projectService.getProjectById(projectId);
        if (project == null) {
           
            return "error"; 
        }

        List<Task> tasks = taskService.getTasksByProjectId(projectId);

        model.addAttribute("project", project);
        model.addAttribute("tasks", tasks);
        model.addAttribute("pageTitle", "Project details | Africa PHEOC-Net");

        return "project-details";
    }


    @GetMapping("/edit/{projectId}")
    public String showEditProjectForm(@PathVariable Long projectId, Model model,Principal principal) {
    	addUserInfoToModel(model, principal);
        
        Project project = projectService.getProjectById(projectId);
        if (project == null) {
          
            return "error"; 
        }

        model.addAttribute("pageTitle", "Edit Project | Africa PHEOC-Net");
        model.addAttribute("project", project);

        
        return "edit-project"; 
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
