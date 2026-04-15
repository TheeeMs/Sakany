package com.theMs.sakany.community.internal.application.queries;

import com.theMs.sakany.community.internal.domain.Announcement;
import com.theMs.sakany.community.internal.domain.AnnouncementRepository;
import com.theMs.sakany.shared.cqrs.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetActiveAnnouncementsQueryHandler implements QueryHandler<GetActiveAnnouncementsQuery, List<Announcement>> {

    private final AnnouncementRepository announcementRepository;

    public GetActiveAnnouncementsQueryHandler(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    @Override
    public List<Announcement> handle(GetActiveAnnouncementsQuery query) {
        return announcementRepository.findActiveAnnouncements();
    }
}
