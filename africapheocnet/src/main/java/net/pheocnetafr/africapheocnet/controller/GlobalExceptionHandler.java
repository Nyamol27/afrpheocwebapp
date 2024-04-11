package net.pheocnetafr.africapheocnet.controller;

import net.pheocnetafr.africapheocnet.exception.EmailSendingException;
import net.pheocnetafr.africapheocnet.exception.TemplateProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailSendingException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleEmailSendingException(EmailSendingException ex, Model model) {
        model.addAttribute("errorCode", ex.getErrorCode());
        return "error/500";
    }

    @ExceptionHandler(TemplateProcessingException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleTemplateProcessingException(TemplateProcessingException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/500";
    }
}

