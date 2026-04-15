package com.theMs.sakany.community.internal.application.commands;

import com.theMs.sakany.community.internal.domain.Announcement;
import com.theMs.sakany.community.internal.domain.AnnouncementRepository;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeactivateAnnouncementCommandHandler implements CommandHandler<DeactivateAnnouncementCommand, Void> {

    private final AnnouncementRepository announcementRepository;

    public DeactivateAnnouncementCommandHandler(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    @Override
    @Transactional
    public Void handle(DeactivateAnnouncementCommand command) {
        Announcement announcement = announcementRepository.findById(command.announcementId())
            .orElseThrow(() -> new NotFoundException("Announcement", command.announcementId()));

        announcement.deactivate();
        announcementRepository.save(announcement);
        return null;
    }
}
