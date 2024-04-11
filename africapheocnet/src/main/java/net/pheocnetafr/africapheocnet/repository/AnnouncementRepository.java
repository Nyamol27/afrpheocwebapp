package net.pheocnetafr.africapheocnet.repository;

import net.pheocnetafr.africapheocnet.entity.Announcement;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByStatus(String status);
    List<Announcement> findAllByOrderByPublicationDateDesc();
}



