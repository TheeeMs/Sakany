package com.theMs.sakany.notifications.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SystemNotificationTemplateJpaRepository extends JpaRepository<SystemNotificationTemplateEntity, UUID> {
    List<SystemNotificationTemplateEntity> findAllByOrderByDisplayOrderAscTitleAsc();

    Optional<SystemNotificationTemplateEntity> findByTemplateKey(String templateKey);
}
