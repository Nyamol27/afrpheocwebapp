package net.pheocnetafr.africapheocnet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import net.pheocnetafr.africapheocnet.entity.ContactForm;
import net.pheocnetafr.africapheocnet.service.EmailService;

@RestController
public class ContactController {
	
	@Autowired
	private EmailService emailService;

	  @PostMapping("/contact")
	    public ResponseEntity<String> sendEmail(@RequestBody ContactForm contactForm) {
	        emailService.sendContactEmail(contactForm);
	        return ResponseEntity.status(HttpStatus.OK).body("Message sent successfully");
	    }
}