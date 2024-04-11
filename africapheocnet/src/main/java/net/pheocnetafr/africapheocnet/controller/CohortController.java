package net.pheocnetafr.africapheocnet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import net.pheocnetafr.africapheocnet.entity.Cohort;
import net.pheocnetafr.africapheocnet.service.CohortService;

import java.util.List;

@Controller
@RequestMapping("/cohorts")
public class CohortController {

    @Autowired
    private CohortService cohortService;

    // View all cohorts
    @GetMapping("/")
    public String viewAllCohorts(Model model) {
        try {
            List<Cohort> cohorts = cohortService.getAllCohorts();
            model.addAttribute("cohorts", cohorts);
            return "cohort/list";
        } catch (Exception e) {
            return handleException(e);
        }
    }

    // View a specific cohort
    @GetMapping("/{id}")
    public String viewCohort(@PathVariable Long id, Model model) {
        try {
            Cohort cohort = cohortService.getCohortById(id);
            model.addAttribute("cohort", cohort);
            return "cohort/view";
        } catch (Exception e) {
            return handleException(e);
        }
    }

    // Create a new cohort
    @PostMapping("/create")
    public String createCohort(@ModelAttribute Cohort cohort) {
        try {
            cohortService.createCohort(cohort);
            return "redirect:/cohorts/";
        } catch (Exception e) {
            return handleException(e);
        }
    }

    // Update a cohort
    @PostMapping("/{id}/update")
    public String updateCohort(@PathVariable Long id, @ModelAttribute Cohort updatedCohort) {
        try {
            cohortService.updateCohort(id, updatedCohort);
            return "redirect:/cohorts/";
        } catch (Exception e) {
            return handleException(e);
        }
    }

    // Delete a cohort
    @PostMapping("/{id}/delete")
    public String deleteCohort(@PathVariable Long id) {
        try {
            cohortService.deleteCohort(id);
            return "redirect:/cohorts/";
        } catch (Exception e) {
            return handleException(e);
        }
    }

    // Handle exceptions
    private String handleException(Exception e) {
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("errorMessage", e.getMessage());
        return "error";
    }
}


