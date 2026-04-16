package com.theMs.sakany.community.internal.application.queries;

import com.theMs.sakany.accounts.internal.infrastructure.persistence.ResidentProfileEntity;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.ResidentProfileJpaRepository;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.UserEntity;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.UserJpaRepository;
import com.theMs.sakany.community.internal.domain.Feedback;
import com.theMs.sakany.community.internal.domain.FeedbackRepository;
import com.theMs.sakany.community.internal.domain.FeedbackStatus;
import com.theMs.sakany.property.internal.infrastructure.persistence.BuildingEntity;
import com.theMs.sakany.property.internal.infrastructure.persistence.BuildingJpaRepository;
import com.theMs.sakany.property.internal.infrastructure.persistence.UnitEntity;
import com.theMs.sakany.property.internal.infrastructure.persistence.UnitJpaRepository;
import com.theMs.sakany.shared.exception.BusinessRuleException;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminResidentFeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserJpaRepository userJpaRepository;
    private final ResidentProfileJpaRepository residentProfileJpaRepository;
    private final UnitJpaRepository unitJpaRepository;
    private final BuildingJpaRepository buildingJpaRepository;

    public AdminResidentFeedbackService(
            FeedbackRepository feedbackRepository,
            UserJpaRepository userJpaRepository,
            ResidentProfileJpaRepository residentProfileJpaRepository,
            UnitJpaRepository unitJpaRepository,
            BuildingJpaRepository buildingJpaRepository
    ) {
        this.feedbackRepository = feedbackRepository;
        this.userJpaRepository = userJpaRepository;
        this.residentProfileJpaRepository = residentProfileJpaRepository;
        this.unitJpaRepository = unitJpaRepository;
        this.buildingJpaRepository = buildingJpaRepository;
    }

    public AdminResidentFeedbackResponse getDashboard(
            String tab,
            String status,
            String category,
            String search
    ) {
        FeedbackVisibility selectedTab = FeedbackVisibility.from(tab);
        String selectedStatus = normalizeStatus(status);
        String normalizedCategory = normalize(category);
        String normalizedSearch = normalize(search);

        List<Feedback> allFeedback = feedbackRepository.findAllOrderByCreatedAtDesc();
        Map<UUID, ResidentContext> residentContextByUser = buildResidentContexts(allFeedback);

        List<Feedback> filtered = allFeedback.stream()
                .filter(item -> selectedTab == FeedbackVisibility.ALL || item.isPublic() == (selectedTab == FeedbackVisibility.PUBLIC))
                .filter(item -> matchesStatus(item, selectedStatus))
                .filter(item -> matchesCategory(item, normalizedCategory))
                .filter(item -> matchesSearch(item, residentContextByUser.get(item.getAuthorId()), normalizedSearch))
                .toList();

        List<FeedbackCardItem> cards = filtered.stream()
                .map(item -> toCard(item, residentContextByUser.get(item.getAuthorId())))
                .toList();

        long publicCount = allFeedback.stream().filter(Feedback::isPublic).count();
        long privateCount = allFeedback.stream().filter(item -> !item.isPublic()).count();

        List<Feedback> summaryScope = allFeedback.stream()
            .filter(item -> selectedTab == FeedbackVisibility.ALL || item.isPublic() == (selectedTab == FeedbackVisibility.PUBLIC))
            .toList();
        long totalSuggestions = summaryScope.size();

        long pendingCount = summaryScope.stream()
            .filter(item -> item.getStatus() == FeedbackStatus.OPEN || item.getStatus() == FeedbackStatus.UNDER_REVIEW)
            .count();
        long totalVotes = summaryScope.stream().mapToLong(item -> (long) item.getUpvotes() + item.getDownvotes()).sum();
        long popularCount = summaryScope.stream()
            .filter(item -> ((long) item.getUpvotes() + item.getDownvotes()) >= 20)
                .count();

        List<FeedbackTabCounter> tabs = List.of(
                new FeedbackTabCounter("PUBLIC", "Public Suggestions", publicCount),
                new FeedbackTabCounter("PRIVATE", "Private Feedback", privateCount)
        );

        return new AdminResidentFeedbackResponse(
                selectedTab.name(),
                selectedStatus,
                tabs,
            new FeedbackSummaryCards(totalSuggestions, pendingCount, totalVotes, popularCount),
                getCategoryOptions(selectedTab == FeedbackVisibility.ALL ? null : selectedTab.name()),
                cards.size(),
                cards
        );
    }

    public List<String> getCategoryOptions(String tab) {
        FeedbackVisibility visibility = tab == null ? FeedbackVisibility.ALL : FeedbackVisibility.from(tab);

        return feedbackRepository.findAllOrderByCreatedAtDesc().stream()
                .filter(item -> visibility == FeedbackVisibility.ALL || item.isPublic() == (visibility == FeedbackVisibility.PUBLIC))
                .map(Feedback::getCategory)
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public List<String> getStatusOptions() {
        return List.of("ALL", "PENDING", "UNDER_REVIEW", "ADDRESSED", "APPROVED", "CLOSED");
    }

    public FeedbackCardItem getDetails(UUID feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new NotFoundException("Feedback", feedbackId));

        Map<UUID, ResidentContext> contextByUser = buildResidentContexts(List.of(feedback));
        return toCard(feedback, contextByUser.get(feedback.getAuthorId()));
    }

    @Transactional
    public void respond(UUID feedbackId, String response, String newStatus) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new NotFoundException("Feedback", feedbackId));

        FeedbackStatus resolvedStatus = resolveUpdateStatus(newStatus);

        if ((response == null || response.isBlank()) && resolvedStatus == null) {
            throw new BusinessRuleException("Either response or newStatus must be provided");
        }

        Feedback updated = Feedback.reconstitute(
                feedback.getId(),
                feedback.getAuthorId(),
                feedback.getTitle(),
                feedback.getContent(),
                feedback.getType(),
                feedback.isPublic(),
                resolvedStatus != null ? resolvedStatus : feedback.getStatus(),
                feedback.getUpvotes(),
                feedback.getDownvotes(),
                feedback.getCategory(),
                feedback.getLocation(),
                feedback.isAnonymous(),
                response == null || response.isBlank() ? feedback.getAdminResponse() : response.trim(),
                feedback.getImageUrl(),
                feedback.getViewCount(),
                feedback.getCreatedAt()
        );

        feedbackRepository.save(updated);
    }

    @Transactional
    public void deleteFeedback(UUID feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new NotFoundException("Feedback", feedbackId));
        feedbackRepository.deleteById(feedback.getId());
    }

    private Map<UUID, ResidentContext> buildResidentContexts(List<Feedback> feedbackItems) {
        Set<UUID> authorIds = feedbackItems.stream().map(Feedback::getAuthorId).collect(Collectors.toSet());
        if (authorIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, UserEntity> usersById = userJpaRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, value -> value));

        List<ResidentProfileEntity> profiles = residentProfileJpaRepository.findAll().stream()
                .filter(profile -> profile.getUser() != null && authorIds.contains(profile.getUser().getId()))
                .toList();

        Set<UUID> unitIds = profiles.stream()
                .map(ResidentProfileEntity::getUnitId)
                .filter(value -> value != null)
                .collect(Collectors.toSet());

        Map<UUID, UnitEntity> unitsById = unitJpaRepository.findAllById(unitIds).stream()
                .collect(Collectors.toMap(UnitEntity::getId, value -> value));

        Set<UUID> buildingIds = unitsById.values().stream()
                .map(UnitEntity::getBuildingId)
                .filter(value -> value != null)
                .collect(Collectors.toSet());

        Map<UUID, BuildingEntity> buildingsById = buildingJpaRepository.findAllById(buildingIds).stream()
                .collect(Collectors.toMap(BuildingEntity::getId, value -> value));

        Map<UUID, ResidentProfileEntity> profileByUserId = profiles.stream()
                .collect(Collectors.toMap(profile -> profile.getUser().getId(), value -> value, (left, right) -> left));

        return authorIds.stream().collect(Collectors.toMap(
                userId -> userId,
                userId -> {
                    UserEntity user = usersById.get(userId);
                    ResidentProfileEntity profile = profileByUserId.get(userId);
                    UnitEntity unit = profile == null || profile.getUnitId() == null ? null : unitsById.get(profile.getUnitId());
                    BuildingEntity building = unit == null ? null : buildingsById.get(unit.getBuildingId());
                    return new ResidentContext(
                            userId,
                            resolveName(user),
                            resolveUnitLabel(unit, building)
                    );
                }
        ));
    }

    private FeedbackCardItem toCard(Feedback feedback, ResidentContext resident) {
        long votes = (long) feedback.getUpvotes() + feedback.getDownvotes();
        String status = toUiStatus(feedback.getStatus());
        String authorName = feedback.isAnonymous() ? "Anonymous Resident" : (resident == null ? "Resident" : resident.fullName());

        return new FeedbackCardItem(
                feedback.getId(),
                feedback.isPublic() ? "PUBLIC" : "PRIVATE",
                feedback.getTitle(),
                feedback.getContent(),
                status,
                feedback.getCategory(),
                feedback.getType().name(),
                feedback.getLocation(),
                feedback.getImageUrl(),
                feedback.getAdminResponse(),
                feedback.getUpvotes(),
                feedback.getDownvotes(),
                votes,
                feedback.getViewCount(),
                authorName,
                resident == null ? null : resident.unitLabel(),
                feedback.getCreatedAt(),
                true,
                true,
                true,
                "/v1/admin/feedback/" + feedback.getId(),
                "/v1/admin/feedback/" + feedback.getId() + "/respond",
                "/v1/admin/feedback/" + feedback.getId()
        );
    }

    private boolean matchesStatus(Feedback item, String selectedStatus) {
        if ("ALL".equals(selectedStatus)) {
            return true;
        }
        return selectedStatus.equalsIgnoreCase(toUiStatus(item.getStatus()));
    }

    private boolean matchesCategory(Feedback item, String normalizedCategory) {
        if (normalizedCategory == null || "all".equals(normalizedCategory)) {
            return true;
        }
        String value = normalize(item.getCategory());
        return normalizedCategory.equals(value);
    }

    private boolean matchesSearch(Feedback item, ResidentContext resident, String normalizedSearch) {
        if (normalizedSearch == null) {
            return true;
        }

        String haystack = (
                safe(item.getTitle()) + " " +
                safe(item.getContent()) + " " +
                safe(item.getCategory()) + " " +
                safe(item.getLocation()) + " " +
                (resident == null ? "" : safe(resident.fullName())) + " " +
                (resident == null ? "" : safe(resident.unitLabel()))
        ).toLowerCase(Locale.ROOT);

        return haystack.contains(normalizedSearch);
    }

    private String resolveName(UserEntity user) {
        if (user == null) {
            return "Resident";
        }
        String fullName = (safe(user.getFirstName()) + " " + safe(user.getLastName())).trim();
        return fullName.isBlank() ? "Resident" : fullName;
    }

    private String resolveUnitLabel(UnitEntity unit, BuildingEntity building) {
        if (unit == null) {
            return null;
        }
        if (building == null || building.getName() == null || building.getName().isBlank()) {
            return unit.getUnitNumber();
        }
        return building.getName() + "-" + unit.getUnitNumber();
    }

    private String toUiStatus(FeedbackStatus status) {
        return switch (status) {
            case OPEN -> "PENDING";
            case UNDER_REVIEW -> "UNDER_REVIEW";
            case ADDRESSED -> "ADDRESSED";
            case APPROVED -> "APPROVED";
            case CLOSED -> "CLOSED";
        };
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim().toLowerCase(Locale.ROOT);
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return "ALL";
        }

        String normalized = raw.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        if ("PENDING_REVIEW".equals(normalized)) {
            return "PENDING";
        }
        if ("IN_REVIEW".equals(normalized)) {
            return "UNDER_REVIEW";
        }
        if ("RESOLVED".equals(normalized)) {
            return "ADDRESSED";
        }
        if ("DONE".equals(normalized)) {
            return "CLOSED";
        }

        if (Set.of("ALL", "PENDING", "UNDER_REVIEW", "ADDRESSED", "APPROVED", "CLOSED").contains(normalized)) {
            return normalized;
        }
        throw new BusinessRuleException("status must be ALL, PENDING, UNDER_REVIEW, ADDRESSED, APPROVED, or CLOSED");
    }

    private FeedbackStatus resolveUpdateStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return null;
        }

        String normalized = rawStatus.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        return switch (normalized) {
            case "PENDING", "OPEN" -> FeedbackStatus.OPEN;
            case "UNDER_REVIEW", "IN_REVIEW" -> FeedbackStatus.UNDER_REVIEW;
            case "ADDRESSED", "RESOLVED" -> FeedbackStatus.ADDRESSED;
            case "APPROVED" -> FeedbackStatus.APPROVED;
            case "CLOSED", "DONE" -> FeedbackStatus.CLOSED;
            default -> throw new BusinessRuleException("newStatus must be PENDING, UNDER_REVIEW, ADDRESSED, APPROVED, or CLOSED");
        };
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    public enum FeedbackVisibility {
        ALL,
        PUBLIC,
        PRIVATE;

        public static FeedbackVisibility from(String raw) {
            if (raw == null || raw.isBlank()) {
                return ALL;
            }
            String normalized = raw.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
            return switch (normalized) {
                case "ALL" -> ALL;
                case "PUBLIC", "PUBLIC_SUGGESTIONS" -> PUBLIC;
                case "PRIVATE", "PRIVATE_FEEDBACK" -> PRIVATE;
                default -> throw new BusinessRuleException("tab must be ALL, PUBLIC, or PRIVATE");
            };
        }
    }

    public record AdminResidentFeedbackResponse(
            String selectedTab,
            String selectedStatus,
            List<FeedbackTabCounter> tabs,
            FeedbackSummaryCards summary,
            List<String> categoryOptions,
            long totalItems,
            List<FeedbackCardItem> items
    ) {
    }

    public record FeedbackTabCounter(
            String key,
            String label,
            long count
    ) {
    }

    public record FeedbackSummaryCards(
            long totalSuggestions,
            long pendingReview,
            long totalVotes,
            long popularCount
    ) {
    }

    public record FeedbackCardItem(
            UUID feedbackId,
            String tab,
            String title,
            String content,
            String status,
            String category,
            String type,
            String location,
            String imageUrl,
            String adminResponse,
            int upvotes,
            int downvotes,
            long totalVotes,
            int viewCount,
            String residentName,
            String residentUnit,
            Instant createdAt,
            boolean canView,
            boolean canRespond,
            boolean canDelete,
            String viewUrl,
            String respondUrl,
            String deleteUrl
    ) {
    }

    private record ResidentContext(
            UUID userId,
            String fullName,
            String unitLabel
    ) {
    }
}
