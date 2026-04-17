package com.theMs.sakany.notifications.internal.application.queries;

import com.theMs.sakany.accounts.internal.infrastructure.persistence.UserEntity;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.UserJpaRepository;
import com.theMs.sakany.community.internal.domain.Announcement;
import com.theMs.sakany.community.internal.domain.AnnouncementPriority;
import com.theMs.sakany.community.internal.domain.AnnouncementRepository;
import com.theMs.sakany.community.internal.infrastructure.persistence.AnnouncementEntity;
import com.theMs.sakany.community.internal.infrastructure.persistence.AnnouncementJpaRepository;
import com.theMs.sakany.notifications.internal.domain.NotificationChannel;
import com.theMs.sakany.notifications.internal.domain.NotificationStatus;
import com.theMs.sakany.notifications.internal.domain.NotificationType;
import com.theMs.sakany.notifications.internal.infrastructure.persistence.NotificationLogEntity;
import com.theMs.sakany.notifications.internal.infrastructure.persistence.NotificationLogJpaRepository;
import com.theMs.sakany.shared.exception.BusinessRuleException;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AdminCommunicationsCenterService {

    private static final Set<NotificationType> SYSTEM_NOTIFICATION_TYPES = EnumSet.of(
            NotificationType.ALERT,
            NotificationType.PAYMENT_DUE,
            NotificationType.MAINTENANCE_UPDATE
    );

    private final NotificationLogJpaRepository notificationLogJpaRepository;
    private final AnnouncementJpaRepository announcementJpaRepository;
    private final AnnouncementRepository announcementRepository;
    private final UserJpaRepository userJpaRepository;

    public AdminCommunicationsCenterService(
            NotificationLogJpaRepository notificationLogJpaRepository,
            AnnouncementJpaRepository announcementJpaRepository,
            AnnouncementRepository announcementRepository,
            UserJpaRepository userJpaRepository
    ) {
        this.notificationLogJpaRepository = notificationLogJpaRepository;
        this.announcementJpaRepository = announcementJpaRepository;
        this.announcementRepository = announcementRepository;
        this.userJpaRepository = userJpaRepository;
    }

    public AdminCommunicationsCenterResponse getCenter(String tab, String status, String search) {
        CommunicationTab selectedTab = CommunicationTab.from(tab);
        String selectedStatus = normalizeStatus(status);
        String normalizedSearch = normalize(search);

        List<NotificationCampaign> pushCampaigns = buildNotificationCampaigns(CommunicationTab.PUSH_NOTIFICATIONS);
        List<NotificationCampaign> systemCampaigns = buildNotificationCampaigns(CommunicationTab.SYSTEM_NOTIFICATIONS);
        List<AnnouncementEntity> announcementEntities = announcementJpaRepository.findAll().stream()
                .sorted(Comparator.comparing(AnnouncementEntity::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        List<CommunicationCardItem> selectedItems = switch (selectedTab) {
            case PUSH_NOTIFICATIONS -> filterNotificationCards(pushCampaigns, selectedStatus, normalizedSearch);
            case SYSTEM_NOTIFICATIONS -> filterNotificationCards(systemCampaigns, selectedStatus, normalizedSearch);
            case NEWS_ANNOUNCEMENTS -> filterAnnouncementCards(announcementEntities, selectedStatus, normalizedSearch);
        };

        List<CommunicationTabCounter> tabCounters = List.of(
                new CommunicationTabCounter("PUSH_NOTIFICATIONS", "Push Notifications", pushCampaigns.size(), String.valueOf(pushCampaigns.size())),
                new CommunicationTabCounter("NEWS_ANNOUNCEMENTS", "News & Announcements", announcementEntities.size(), String.valueOf(announcementEntities.size())),
                new CommunicationTabCounter(
                        "SYSTEM_NOTIFICATIONS",
                        "System Notifications",
                        systemCampaigns.size(),
                        countSentCampaigns(systemCampaigns) + "/" + systemCampaigns.size()
                )
        );

        List<CommunicationStatusCounter> statusCounters = switch (selectedTab) {
            case PUSH_NOTIFICATIONS, SYSTEM_NOTIFICATIONS -> buildNotificationStatusCounters(
                    selectedTab == CommunicationTab.PUSH_NOTIFICATIONS ? pushCampaigns : systemCampaigns
            );
            case NEWS_ANNOUNCEMENTS -> buildAnnouncementStatusCounters(announcementEntities);
        };

        return new AdminCommunicationsCenterResponse(
                selectedTab.name(),
                selectedStatus,
                tabCounters,
                statusCounters,
                selectedItems.size(),
                selectedItems
        );
    }

    public CommunicationCardItem getNotificationItem(UUID itemId, String tab) {
        CommunicationTab selectedTab = CommunicationTab.from(tab);
        if (selectedTab == CommunicationTab.NEWS_ANNOUNCEMENTS) {
            throw new BusinessRuleException("Notification item lookup supports PUSH_NOTIFICATIONS or SYSTEM_NOTIFICATIONS");
        }

        return buildNotificationCampaigns(selectedTab).stream()
                .filter(campaign -> campaign.itemId().equals(itemId))
                .findFirst()
                .map(this::toCommunicationCard)
                .orElseThrow(() -> new NotFoundException("CommunicationNotification", itemId));
    }

    @Transactional
    public void updateNotificationItem(UUID itemId, String tab, String title, String message, String priority, Boolean sendNow) {
        CommunicationTab selectedTab = CommunicationTab.from(tab);
        if (selectedTab == CommunicationTab.NEWS_ANNOUNCEMENTS) {
            throw new BusinessRuleException("Notification item update supports PUSH_NOTIFICATIONS or SYSTEM_NOTIFICATIONS");
        }

        NotificationCampaign campaign = buildNotificationCampaigns(selectedTab).stream()
                .filter(candidate -> candidate.itemId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("CommunicationNotification", itemId));

        if (!campaign.canEdit()) {
            throw new BusinessRuleException("Only draft/scheduled notifications can be edited");
        }

        List<NotificationLogEntity> entities = notificationLogJpaRepository.findAllById(campaign.notificationLogIds());
        if (entities.isEmpty()) {
            throw new NotFoundException("NotificationCampaignRows", itemId);
        }

        NotificationType mappedType = mapPriorityToNotificationType(priority, selectedTab);

        for (NotificationLogEntity entity : entities) {
            if (title != null && !title.isBlank()) {
                entity.setTitle(title.trim());
            }
            if (message != null && !message.isBlank()) {
                entity.setBody(message.trim());
            }
            if (mappedType != null) {
                entity.setType(mappedType);
            }
            if (Boolean.TRUE.equals(sendNow)) {
                entity.setStatus(NotificationStatus.SENT);
                entity.setSentAt(Instant.now());
                entity.setFailureReason(null);
            }
        }

        notificationLogJpaRepository.saveAll(entities);
    }

    @Transactional
    public void deleteNotificationItem(UUID itemId, String tab) {
        CommunicationTab selectedTab = CommunicationTab.from(tab);
        if (selectedTab == CommunicationTab.NEWS_ANNOUNCEMENTS) {
            throw new BusinessRuleException("Notification item delete supports PUSH_NOTIFICATIONS or SYSTEM_NOTIFICATIONS");
        }

        NotificationCampaign campaign = buildNotificationCampaigns(selectedTab).stream()
                .filter(candidate -> candidate.itemId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("CommunicationNotification", itemId));

        notificationLogJpaRepository.deleteAllById(campaign.notificationLogIds());
    }

    @Transactional
    public CreatePushNotificationResult createPushNotification(
            UUID requestingAdminId,
            List<UUID> recipientIds,
            Boolean sendToAll,
            String title,
            String message,
            String priority,
            Instant scheduleAt
    ) {
        if (title == null || title.isBlank()) {
            throw new BusinessRuleException("title is required");
        }
        if (message == null || message.isBlank()) {
            throw new BusinessRuleException("message is required");
        }

        List<UUID> targets = resolveTargets(recipientIds, sendToAll);
        if (targets.isEmpty()) {
            throw new BusinessRuleException("No target recipients provided");
        }

        boolean shouldSendNow = scheduleAt == null || !scheduleAt.isAfter(Instant.now());
        NotificationStatus status = shouldSendNow ? NotificationStatus.SENT : NotificationStatus.PENDING;
        Instant sentAt = shouldSendNow ? Instant.now() : null;

        UUID campaignReferenceId = UUID.randomUUID();
        NotificationType type = mapPriorityToNotificationType(priority, CommunicationTab.PUSH_NOTIFICATIONS);
        if (type == null) {
            type = NotificationType.GENERAL;
        }

        List<NotificationLogEntity> entities = new ArrayList<>();
        for (UUID recipientId : targets) {
            NotificationLogEntity entity = new NotificationLogEntity(
                    recipientId,
                    title.trim(),
                    message.trim(),
                    type,
                    campaignReferenceId,
                    NotificationChannel.PUSH,
                    status,
                    sentAt,
                    null,
                    null
            );
            entity.setId(UUID.randomUUID());
            entities.add(entity);
        }

        notificationLogJpaRepository.saveAll(entities);

        String sender = resolveUserDisplayName(requestingAdminId);
        UUID campaignItemId = campaignItemId(campaignReferenceId, title, message, type, NotificationChannel.PUSH, sentAt == null ? Instant.now() : sentAt);

        return new CreatePushNotificationResult(
                campaignItemId,
                entities.size(),
                shouldSendNow ? "SENT" : "SCHEDULED",
                sender
        );
    }

    public CommunicationCardItem getAnnouncementItem(UUID announcementId) {
        AnnouncementEntity entity = announcementJpaRepository.findById(announcementId)
                .orElseThrow(() -> new NotFoundException("Announcement", announcementId));
        return toAnnouncementCard(entity, userJpaRepository.count());
    }

    @Transactional
    public void updateAnnouncementItem(UUID announcementId, String title, String content, String priority, Instant expiresAt, Boolean active) {
        AnnouncementEntity entity = announcementJpaRepository.findById(announcementId)
                .orElseThrow(() -> new NotFoundException("Announcement", announcementId));

        if (title != null && !title.isBlank()) {
            entity.setTitle(title.trim());
        }
        if (content != null && !content.isBlank()) {
            entity.setContent(content.trim());
        }
        if (priority != null && !priority.isBlank()) {
            entity.setPriority(mapAnnouncementPriority(priority));
        }
        if (expiresAt != null) {
            entity.setExpiresAt(expiresAt);
        }
        if (active != null) {
            entity.setActive(active);
        }

        announcementJpaRepository.save(entity);
    }

    @Transactional
    public void deleteAnnouncementItem(UUID announcementId) {
        AnnouncementEntity entity = announcementJpaRepository.findById(announcementId)
                .orElseThrow(() -> new NotFoundException("Announcement", announcementId));
        announcementJpaRepository.delete(entity);
    }

    @Transactional
    public UUID createAnnouncement(
            UUID authorId,
            String title,
            String content,
            String priority,
            Instant expiresAt
    ) {
        AnnouncementPriority announcementPriority = mapAnnouncementPriority(priority);
        Announcement announcement = Announcement.create(authorId, title, content, announcementPriority, expiresAt);
        return announcementRepository.save(announcement).getId();
    }

    private List<CommunicationCardItem> filterNotificationCards(List<NotificationCampaign> campaigns, String status, String search) {
        return campaigns.stream()
                .filter(campaign -> matchesNotificationStatus(campaign, status))
                .filter(campaign -> matchesSearch(campaign.title(), campaign.message(), search))
                .map(this::toCommunicationCard)
                .toList();
    }

    private List<CommunicationCardItem> filterAnnouncementCards(List<AnnouncementEntity> entities, String status, String search) {
        long totalUsers = userJpaRepository.count();
        return entities.stream()
                .filter(entity -> matchesAnnouncementStatus(entity, status))
                .filter(entity -> matchesSearch(entity.getTitle(), entity.getContent(), search))
                .map(entity -> toAnnouncementCard(entity, totalUsers))
                .toList();
    }

    private List<CommunicationStatusCounter> buildNotificationStatusCounters(List<NotificationCampaign> campaigns) {
        long sent = campaigns.stream().filter(campaign -> !campaign.isDraft()).count();
        long scheduled = campaigns.stream().filter(NotificationCampaign::isDraft).count();
        return List.of(
                new CommunicationStatusCounter("ALL", "All", campaigns.size()),
                new CommunicationStatusCounter("INSTANT_SENT", "Instant & Sent", sent),
                new CommunicationStatusCounter("SCHEDULED", "Scheduled", scheduled)
        );
    }

    private List<CommunicationStatusCounter> buildAnnouncementStatusCounters(List<AnnouncementEntity> entities) {
        long sent = entities.stream().filter(AnnouncementEntity::isActive).count();
        long draft = entities.stream().filter(entity -> !entity.isActive()).count();
        return List.of(
                new CommunicationStatusCounter("ALL", "All", entities.size()),
                new CommunicationStatusCounter("SENT", "Sent", sent),
                new CommunicationStatusCounter("DRAFT", "Draft", draft)
        );
    }

    private boolean matchesNotificationStatus(NotificationCampaign campaign, String status) {
        return switch (status) {
            case "ALL" -> true;
            case "INSTANT_SENT" -> !campaign.isDraft();
            case "SCHEDULED" -> campaign.isDraft();
            default -> true;
        };
    }

    private boolean matchesAnnouncementStatus(AnnouncementEntity entity, String status) {
        return switch (status) {
            case "ALL" -> true;
            case "SENT" -> entity.isActive();
            case "DRAFT" -> !entity.isActive();
            default -> true;
        };
    }

    private boolean matchesSearch(String title, String body, String normalizedSearch) {
        if (normalizedSearch == null) {
            return true;
        }

        String composite = (title == null ? "" : title) + " " + (body == null ? "" : body);
        return composite.toLowerCase(Locale.ROOT).contains(normalizedSearch);
    }

    private List<NotificationCampaign> buildNotificationCampaigns(CommunicationTab tab) {
        List<NotificationLogEntity> candidates = notificationLogJpaRepository.findAll().stream()
                .filter(entity -> belongsToTab(entity, tab))
                .toList();

        Map<UUID, List<NotificationLogEntity>> grouped = new LinkedHashMap<>();
        for (NotificationLogEntity entity : candidates) {
            UUID campaignId = campaignItemId(
                    entity.getReferenceId(),
                    entity.getTitle(),
                    entity.getBody(),
                    entity.getType(),
                    entity.getChannel(),
                    notificationAnchor(entity)
            );
            grouped.computeIfAbsent(campaignId, ignored -> new ArrayList<>()).add(entity);
        }

        List<NotificationCampaign> campaigns = new ArrayList<>();
        for (Map.Entry<UUID, List<NotificationLogEntity>> entry : grouped.entrySet()) {
            List<NotificationLogEntity> rows = entry.getValue();
            NotificationLogEntity sample = rows.getFirst();

            boolean draft = rows.stream().allMatch(row -> row.getStatus() == NotificationStatus.PENDING && row.getSentAt() == null);
            long recipientCount = rows.stream().map(NotificationLogEntity::getRecipientId).distinct().count();
            long readCount = rows.stream().filter(row -> row.getStatus() == NotificationStatus.READ).count();
            int readPercent = recipientCount == 0 ? 0 : (int) Math.round((readCount * 100.0) / recipientCount);
            Instant latestSentAt = rows.stream()
                    .map(row -> row.getSentAt() == null ? row.getCreatedAt() : row.getSentAt())
                    .filter(value -> value != null)
                    .max(Comparator.naturalOrder())
                    .orElse(sample.getCreatedAt());

            campaigns.add(new NotificationCampaign(
                    entry.getKey(),
                    tab,
                    sample.getTitle(),
                    sample.getBody(),
                    draft ? "Draft" : "Sent",
                    priorityLabel(sample.getType()),
                    recipientCount,
                    readCount,
                    readPercent,
                    latestSentAt,
                    "System",
                    true,
                    draft,
                    true,
                    rows.stream().map(NotificationLogEntity::getId).toList(),
                    sample.getType(),
                    sample.getChannel(),
                    sample.getReferenceId()
            ));
        }

        campaigns.sort(Comparator.comparing(NotificationCampaign::sentAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return campaigns;
    }

    private CommunicationCardItem toCommunicationCard(NotificationCampaign campaign) {
        String baseUrl = "/v1/admin/communications/notifications/" + campaign.itemId() + "?tab=" + campaign.tab().name();
        return new CommunicationCardItem(
                campaign.itemId(),
                campaign.tab().name(),
                campaign.title(),
                campaign.message(),
                campaign.status(),
                campaign.priority(),
                campaign.recipientCount(),
                campaign.readCount(),
                campaign.readPercent(),
                campaign.sentAt(),
                campaign.sentBy(),
                campaign.canView(),
                campaign.canEdit(),
                campaign.canDelete(),
                baseUrl,
                baseUrl,
                baseUrl
        );
    }

    private CommunicationCardItem toAnnouncementCard(AnnouncementEntity entity, long totalUsers) {
        String status = entity.isActive() ? "Sent" : "Draft";
        String baseUrl = "/v1/admin/communications/announcements/" + entity.getId();

        return new CommunicationCardItem(
                entity.getId(),
                CommunicationTab.NEWS_ANNOUNCEMENTS.name(),
                entity.getTitle(),
                entity.getContent(),
                status,
                mapAnnouncementPriorityLabel(entity.getPriority()),
                entity.isActive() ? totalUsers : 0,
                0,
                0,
                entity.getCreatedAt(),
                resolveUserDisplayName(entity.getAuthorId()),
                true,
                true,
                true,
                baseUrl,
                baseUrl,
                baseUrl
        );
    }

    private boolean belongsToTab(NotificationLogEntity entity, CommunicationTab tab) {
        if (tab == CommunicationTab.PUSH_NOTIFICATIONS) {
            return entity.getChannel() == NotificationChannel.PUSH
                    && entity.getType() != NotificationType.ANNOUNCEMENT
                    && !SYSTEM_NOTIFICATION_TYPES.contains(entity.getType());
        }

        if (tab == CommunicationTab.SYSTEM_NOTIFICATIONS) {
            return SYSTEM_NOTIFICATION_TYPES.contains(entity.getType()) || entity.getChannel() != NotificationChannel.PUSH;
        }

        return false;
    }

    private long countSentCampaigns(List<NotificationCampaign> campaigns) {
        return campaigns.stream().filter(campaign -> !campaign.isDraft()).count();
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim().toLowerCase(Locale.ROOT);
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return "ALL";
        }
        return rawStatus.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
    }

    private UUID campaignItemId(
            UUID referenceId,
            String title,
            String body,
            NotificationType type,
            NotificationChannel channel,
            Instant anchor
    ) {
        Instant bucket = anchor == null ? Instant.EPOCH : anchor.truncatedTo(ChronoUnit.MINUTES);
        String key = (referenceId == null ? "NO_REF" : referenceId.toString())
                + "|" + safe(title)
                + "|" + safe(body)
                + "|" + (type == null ? "GENERAL" : type.name())
                + "|" + (channel == null ? NotificationChannel.PUSH.name() : channel.name())
                + "|" + bucket;

        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }

    private Instant notificationAnchor(NotificationLogEntity entity) {
        if (entity.getSentAt() != null) {
            return entity.getSentAt();
        }
        return entity.getCreatedAt();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private List<UUID> resolveTargets(List<UUID> recipientIds, Boolean sendToAll) {
        if (Boolean.TRUE.equals(sendToAll)) {
            return userJpaRepository.findAll().stream().map(UserEntity::getId).toList();
        }

        if (recipientIds == null) {
            return List.of();
        }

        return recipientIds.stream().distinct().toList();
    }

    private String resolveUserDisplayName(UUID userId) {
        if (userId == null) {
            return "System";
        }

        UserEntity entity = userJpaRepository.findById(userId).orElse(null);
        if (entity == null) {
            return "System";
        }

        String fullName = ((entity.getFirstName() == null ? "" : entity.getFirstName()) + " "
                + (entity.getLastName() == null ? "" : entity.getLastName())).trim();
        return fullName.isBlank() ? "System" : fullName;
    }

    private AnnouncementPriority mapAnnouncementPriority(String rawPriority) {
        if (rawPriority == null || rawPriority.isBlank()) {
            return AnnouncementPriority.NORMAL;
        }

        String normalized = rawPriority.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "LOW" -> AnnouncementPriority.LOW;
            case "NORMAL", "MEDIUM" -> AnnouncementPriority.NORMAL;
            case "URGENT", "HIGH" -> AnnouncementPriority.URGENT;
            default -> throw new BusinessRuleException("priority must be LOW, NORMAL, or HIGH");
        };
    }

    private NotificationType mapPriorityToNotificationType(String rawPriority, CommunicationTab tab) {
        if (rawPriority == null || rawPriority.isBlank()) {
            return null;
        }

        String normalized = rawPriority.trim().toUpperCase(Locale.ROOT);
        if ("HIGH".equals(normalized) || "URGENT".equals(normalized)) {
            return tab == CommunicationTab.SYSTEM_NOTIFICATIONS ? NotificationType.MAINTENANCE_UPDATE : NotificationType.ALERT;
        }
        if ("NORMAL".equals(normalized) || "MEDIUM".equals(normalized)) {
            return tab == CommunicationTab.SYSTEM_NOTIFICATIONS ? NotificationType.MAINTENANCE_UPDATE : NotificationType.EVENT_REMINDER;
        }
        if ("LOW".equals(normalized)) {
            return NotificationType.GENERAL;
        }

        throw new BusinessRuleException("priority must be LOW, NORMAL, or HIGH");
    }

    private String mapAnnouncementPriorityLabel(AnnouncementPriority priority) {
        return switch (priority) {
            case LOW -> "LOW";
            case NORMAL -> "NORMAL";
            case URGENT -> "HIGH";
        };
    }

    private String priorityLabel(NotificationType type) {
        return switch (type) {
            case ALERT, PAYMENT_DUE, MAINTENANCE_UPDATE -> "HIGH";
            case GENERAL -> "LOW";
            default -> "NORMAL";
        };
    }

    public enum CommunicationTab {
        PUSH_NOTIFICATIONS,
        NEWS_ANNOUNCEMENTS,
        SYSTEM_NOTIFICATIONS;

        public static CommunicationTab from(String raw) {
            if (raw == null || raw.isBlank()) {
                return PUSH_NOTIFICATIONS;
            }

            String normalized = raw.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
            return switch (normalized) {
                case "PUSH", "PUSH_NOTIFICATIONS" -> PUSH_NOTIFICATIONS;
                case "NEWS", "ANNOUNCEMENTS", "NEWS_ANNOUNCEMENTS" -> NEWS_ANNOUNCEMENTS;
                case "SYSTEM", "SYSTEM_NOTIFICATIONS" -> SYSTEM_NOTIFICATIONS;
                default -> throw new BusinessRuleException("tab must be PUSH_NOTIFICATIONS, NEWS_ANNOUNCEMENTS, or SYSTEM_NOTIFICATIONS");
            };
        }
    }

    public record AdminCommunicationsCenterResponse(
            String selectedTab,
            String selectedStatus,
            List<CommunicationTabCounter> tabs,
            List<CommunicationStatusCounter> statuses,
            long totalItems,
            List<CommunicationCardItem> items
    ) {
    }

    public record CommunicationTabCounter(
            String key,
            String label,
            long count,
            String badgeText
    ) {
    }

    public record CommunicationStatusCounter(
            String key,
            String label,
            long count
    ) {
    }

    public record CommunicationCardItem(
            UUID itemId,
            String source,
            String title,
            String message,
            String status,
            String priority,
            long recipientCount,
            long readCount,
            int readPercent,
            Instant sentAt,
            String sentBy,
            boolean canView,
            boolean canEdit,
            boolean canDelete,
            String viewUrl,
            String editUrl,
            String deleteUrl
    ) {
    }

    public record CreatePushNotificationResult(
            UUID campaignId,
            int notificationsCreated,
            String deliveryState,
            String sentBy
    ) {
    }

    private record NotificationCampaign(
            UUID itemId,
            CommunicationTab tab,
            String title,
            String message,
            String status,
            String priority,
            long recipientCount,
            long readCount,
            int readPercent,
            Instant sentAt,
            String sentBy,
            boolean canView,
            boolean canEdit,
            boolean canDelete,
            List<UUID> notificationLogIds,
            NotificationType type,
            NotificationChannel channel,
            UUID referenceId
    ) {
        boolean isDraft() {
            return "Draft".equalsIgnoreCase(status);
        }
    }
}
