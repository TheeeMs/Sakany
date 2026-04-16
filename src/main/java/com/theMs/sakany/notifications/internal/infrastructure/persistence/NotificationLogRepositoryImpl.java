package com.theMs.sakany.notifications.internal.infrastructure.persistence;

import com.theMs.sakany.notifications.internal.domain.NotificationLog;
import com.theMs.sakany.notifications.internal.domain.NotificationLogRepository;
import com.theMs.sakany.notifications.internal.domain.NotificationStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
public class NotificationLogRepositoryImpl implements NotificationLogRepository {

    private final NotificationLogJpaRepository jpaRepository;
    private final NotificationLogMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    public NotificationLogRepositoryImpl(
            NotificationLogJpaRepository jpaRepository,
            NotificationLogMapper mapper,
            ApplicationEventPublisher eventPublisher
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public NotificationLog save(NotificationLog notificationLog) {
        Objects.requireNonNull(notificationLog, "NotificationLog cannot be null");

        NotificationLogEntity entity = notificationLog.getId() == null
            ? new NotificationLogEntity()
            : jpaRepository.findById(notificationLog.getId()).orElseGet(NotificationLogEntity::new);

        entity.setId(notificationLog.getId());
        entity.setRecipientId(notificationLog.getRecipientId());
        entity.setTitle(notificationLog.getTitle());
        entity.setBody(notificationLog.getBody());
        entity.setType(notificationLog.getType());
        entity.setReferenceId(notificationLog.getReferenceId());
        entity.setChannel(notificationLog.getChannel());
        entity.setStatus(notificationLog.getStatus());
        entity.setSentAt(notificationLog.getSentAt());
        entity.setReadAt(notificationLog.getReadAt());
        entity.setFailureReason(notificationLog.getFailureReason());

        NotificationLogEntity savedEntity = jpaRepository.save(entity);

        notificationLog.getDomainEvents().forEach(eventPublisher::publishEvent);
        notificationLog.clearDomainEvents();

        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<NotificationLog> findById(UUID id) {
        Objects.requireNonNull(id, "NotificationLog id cannot be null");
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<NotificationLog> findByRecipientId(UUID recipientId) {
        return jpaRepository.findByRecipientId(recipientId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<NotificationLog> findByRecipientIdAndStatus(UUID recipientId, NotificationStatus status) {
        return jpaRepository.findByRecipientIdAndStatus(recipientId, status).stream().map(mapper::toDomain).toList();
    }
}
