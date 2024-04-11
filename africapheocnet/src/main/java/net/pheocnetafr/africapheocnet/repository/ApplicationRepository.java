package net.pheocnetafr.africapheocnet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.pheocnetafr.africapheocnet.entity.Application;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByEmail(String email);
    
    List<Application> findByStatus(String status);
    
    Application findByEmailAndStatus(String email, String status);
}

