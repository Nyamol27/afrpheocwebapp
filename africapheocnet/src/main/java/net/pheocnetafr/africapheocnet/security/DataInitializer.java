package net.pheocnetafr.africapheocnet.security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import net.pheocnetafr.africapheocnet.entity.User;
import net.pheocnetafr.africapheocnet.repository.UserRepository;

@Configuration
public class DataInitializer {

	@Bean
	CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
	    return args -> {
	        String defaultUsername1 = "kaweyan@gmail.com";
	        if (userRepository.findByEmail(defaultUsername1) == null) {
	            User user1 = new User();
	            user1.setEmail(defaultUsername1);
	            user1.setPassword(passwordEncoder.encode("September27th!")); 
	            user1.setFirstName("Yan");
	            user1.setLastName("Kawe");
	            user1.setRole("ADMIN"); 
	            userRepository.save(user1);
	            System.out.println("Default user 1 with role created");
	        } else {
	            System.out.println("Default user 1 already exists");
	        }

	        String defaultUsername2 = "kawey@who.int";
	        if (userRepository.findByEmail(defaultUsername2) == null) {
	            User user2 = new User();
	            user2.setEmail(defaultUsername2);
	            user2.setPassword(passwordEncoder.encode("September27th!")); 
	            user2.setFirstName("Another");
	            user2.setLastName("User");
	            user2.setRole("USER");  
	            userRepository.save(user2);
	            System.out.println("Default user 2 with role created");
	        } else {
	            System.out.println("Default user 2 already exists");
	        }
	    };
	}

}

