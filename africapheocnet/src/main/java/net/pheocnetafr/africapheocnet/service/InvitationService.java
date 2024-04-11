package net.pheocnetafr.africapheocnet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import net.pheocnetafr.africapheocnet.entity.Invitation;
import net.pheocnetafr.africapheocnet.repository.InvitationRepository;

@Service
public class InvitationService {
    private final InvitationRepository invitationRepository;

    @Autowired
    public InvitationService(InvitationRepository invitationRepository) {
        this.invitationRepository = invitationRepository;
    }

    public void createInvitation(Invitation invitation) {
        invitationRepository.save(invitation);
    }

    public boolean isEmailAlreadyInvited(String email) {
        return invitationRepository.existsByReceiverEmail(email);
    }

}
