package net.pheocnetafr.africapheocnet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@ComponentScan(basePackages = {"net.pheocnetafr.africapheocnet.security", "net.pheocnetafr.africapheocnet.forum", "net.pheocnetafr.africapheocnet.controller","net.pheocnetafr.africapheocnet.service","net.pheocnetafr.africapheocnet.entity"})
public class AfricapheocnetApplication {

    public static void main(String[] args) {
        SpringApplication.run(AfricapheocnetApplication.class, args);
    }

}
