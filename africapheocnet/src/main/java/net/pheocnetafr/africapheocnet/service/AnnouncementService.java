package net.pheocnetafr.africapheocnet.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.pheocnetafr.africapheocnet.entity.Announcement;
import net.pheocnetafr.africapheocnet.entity.Notification;
import net.pheocnetafr.africapheocnet.repository.AnnouncementRepository;
import net.pheocnetafr.africapheocnet.repository.NotificationRepository;

@Service
public class AnnouncementService {

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationRepository notificationRepository;

    public void addAnnouncementWithAttachment(Announcement announcement, MultipartFile attachment) throws IOException {
        // Save the announcement in the database
        Announcement savedAnnouncement = announcementRepository.save(announcement);

        // Send the announcement email to all enabled users with attachment
        emailService.sendAnnouncementEmailWithAttachment(savedAnnouncement, attachment);
    }

    public void addAnnouncementWithoutAttachment(Announcement announcement) {
        // Save the announcement in the database
        Announcement savedAnnouncement = announcementRepository.save(announcement);

        // Send the announcement email to all enabled users without attachment
        emailService.sendAnnouncementEmail(savedAnnouncement);
    }


    public List<Announcement> getAllAnnouncements() {
        return announcementRepository.findAll();
    }

    public Announcement getAnnouncementById(Long id) {
        return announcementRepository.findById(id).orElse(null);
    }

    public Announcement updateAnnouncement(Long id, Announcement updatedAnnouncement, MultipartFile attachmentFile) throws IOException {
        Announcement existingAnnouncement = announcementRepository.findById(id).orElse(null);
        if (existingAnnouncement != null) {
            existingAnnouncement.setSubject(updatedAnnouncement.getSubject());
            existingAnnouncement.setContent(updatedAnnouncement.getContent());
            existingAnnouncement.setPublicationDate(updatedAnnouncement.getPublicationDate());
            existingAnnouncement.setStatus(updatedAnnouncement.getStatus());

            // Update the attachment if a new file is provided
            if (attachmentFile != null && !attachmentFile.isEmpty()) {
                existingAnnouncement.setAttachment(attachmentFile.getBytes());
            }

            return announcementRepository.save(existingAnnouncement);
        }
        return null;
    }

    public void deleteAnnouncement(Long id) {
        announcementRepository.deleteById(id);
    }

   

    public long getTotalAnnouncementsCount() {
        return announcementRepository.count();
    }

    public List<Announcement> getAnnouncementsByPage(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("publicationDate").descending());
        Page<Announcement> pageResult = announcementRepository.findAll(pageable);
        return pageResult.getContent();
    }
    
    public List<Announcement> getAllAnnouncementsSortedByPublicationDateDesc() {
        return announcementRepository.findAllByOrderByPublicationDateDesc();
    }
    
    public Optional<Announcement> findById(Long id) {
        return announcementRepository.findById(id);
    }
    
    public Announcement save(Announcement announcement) {
        return announcementRepository.save(announcement);
    }
}
