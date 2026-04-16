package com.theMs.sakany.community.internal.infrastructure.persistence;

import com.theMs.sakany.community.internal.domain.Announcement;
import com.theMs.sakany.community.internal.domain.AnnouncementRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class AnnouncementRepositoryImpl implements AnnouncementRepository {

    private final AnnouncementJpaRepository jpaRepository;
    private final AnnouncementMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    public AnnouncementRepositoryImpl(AnnouncementJpaRepository jpaRepository, AnnouncementMapper mapper, ApplicationEventPublisher eventPublisher) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Announcement save(Announcement announcement) {
        AnnouncementEntity entity;
        if (announcement.getId() != null && jpaRepository.existsById(announcement.getId())) {
             entity = jpaRepository.findById(announcement.getId()).orElseThrow();
             // Map fields from domain to existing entity
             entity.setTitle(announcement.getTitle());
             entity.setContent(announcement.getContent());
             entity.setPriority(announcement.getPriority());
             entity.setActive(announcement.isActive());
             entity.setExpiresAt(announcement.getExpiresAt());
        } else {
             entity = mapper.toEntity(announcement);
        }
        
        AnnouncementEntity savedEntity = jpaRepository.save(entity);

        // Publish events
        announcement.getDomainEvents().forEach(eventPublisher::publishEvent);
        announcement.clearDomainEvents();

        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Announcement> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Announcement> findActiveAnnouncements() {
        return jpaRepository.findByIsActiveTrueOrderByCreatedAtDesc().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
