package com.theMs.sakany.community.internal.application.commands;

import com.theMs.sakany.community.internal.domain.Announcement;
import com.theMs.sakany.community.internal.domain.AnnouncementRepository;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CreateAnnouncementCommandHandler implements CommandHandler<CreateAnnouncementCommand, UUID> {

    private final AnnouncementRepository announcementRepository;

    public CreateAnnouncementCommandHandler(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    @Override
    @Transactional
    public UUID handle(CreateAnnouncementCommand command) {
        Announcement announcement = Announcement.create(
            command.authorId(),
            command.title(),
            command.content(),
            command.priority(),
            command.expiresAt()
        );
        Announcement savedAnnouncement = announcementRepository.save(announcement);
        return savedAnnouncement.getId();
    }
}
