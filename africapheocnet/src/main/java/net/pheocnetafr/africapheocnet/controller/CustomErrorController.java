package net.pheocnetafr.africapheocnet.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @GetMapping("/error")
    public String handleError(WebRequest webRequest, Model model) {
        // Get the error message from the request attribute
        String errorMessage = (String) webRequest.getAttribute("javax.servlet.error.message", RequestAttributes.SCOPE_REQUEST);

        // Add the error message to the Thymeleaf model
        model.addAttribute("errorMessage", errorMessage);

        // Return the error Thymeleaf template
        return "error";
    }

    public String getErrorPath() {
        return "/error";
    }
}
