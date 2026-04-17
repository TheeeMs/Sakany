package com.theMs.sakany.community.internal.application.queries;

import com.theMs.sakany.community.internal.application.commands.SubmitFeedbackCommand;
import com.theMs.sakany.community.internal.application.commands.UpdateFeedbackStatusCommand;
import com.theMs.sakany.community.internal.domain.FeedbackStatus;
import com.theMs.sakany.community.internal.domain.FeedbackType;
import com.theMs.sakany.community.internal.infrastructure.persistence.AdminFeedbackDashboardRow;
import com.theMs.sakany.community.internal.infrastructure.persistence.AdminFeedbackSummaryRow;
import com.theMs.sakany.community.internal.infrastructure.persistence.FeedbackJpaRepository;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import com.theMs.sakany.shared.exception.BusinessRuleException;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AdminFeedbackDashboardService {

    private static final int DEFAULT_PAGE_SIZE = 25;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int POPULAR_THRESHOLD = 20;
    private static final List<String> DEFAULT_CATEGORIES = List.of(
            "Facilities",
            "Maintenance",
            "Security",
            "Events & Activities",
            "Services",
            "Other"
    );

    private final FeedbackJpaRepository feedbackJpaRepository;
    private final CommandHandler<SubmitFeedbackCommand, UUID> submitFeedbackHandler;
    private final CommandHandler<UpdateFeedbackStatusCommand, Void> updateFeedbackStatusHandler;

    public AdminFeedbackDashboardService(
            FeedbackJpaRepository feedbackJpaRepository,
            CommandHandler<SubmitFeedbackCommand, UUID> submitFeedbackHandler,
            CommandHandler<UpdateFeedbackStatusCommand, Void> updateFeedbackStatusHandler
    ) {
        this.feedbackJpaRepository = feedbackJpaRepository;
        this.submitFeedbackHandler = submitFeedbackHandler;
        this.updateFeedbackStatusHandler = updateFeedbackStatusHandler;
    }

    public AdminFeedbackDashboardResponse getDashboard(
            AdminFeedbackTab tab,
            String search,
            AdminFeedbackStatusFilter status,
            String category,
            int page,
            int size
    ) {
        AdminFeedbackTab effectiveTab = tab == null ? AdminFeedbackTab.PUBLIC_SUGGESTIONS : tab;
        AdminFeedbackStatusFilter effectiveStatus = status == null ? AdminFeedbackStatusFilter.ALL : status;

        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);

        List<AdminFeedbackCardItem> filteredItems = feedbackJpaRepository.findAdminFeedbackRows(
                        resolvePublicFilter(effectiveTab),
                        resolveTypeFilter(effectiveTab)
                ).stream()
                .map(this::mapRow)
                .filter(item -> matchesSearch(item, search))
                .filter(item -> matchesStatus(item, effectiveStatus))
                .filter(item -> matchesCategory(item, category))
                .toList();

        long totalElements = filteredItems.size();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil(totalElements / (double) safeSize);
        int fromIndex = Math.min(safePage * safeSize, filteredItems.size());
        int toIndex = Math.min(fromIndex + safeSize, filteredItems.size());

        List<AdminFeedbackCardItem> pageItems = filteredItems.subList(fromIndex, toIndex);

        AdminFeedbackSummary summary = mapSummary(feedbackJpaRepository.getAdminFeedbackSummary());

        return new AdminFeedbackDashboardResponse(
                pageItems,
                safePage,
                safeSize,
                totalElements,
                totalPages,
                toIndex < filteredItems.size(),
                safePage > 0,
                summary,
                new AdminFeedbackTabCounters(summary.publicSuggestionsCount(), summary.privateFeedbackCount())
        );
    }

    public List<String> getStatusOptions() {
        return Arrays.stream(AdminFeedbackStatusFilter.values()).map(Enum::name).toList();
    }

    public List<String> getCategoryOptions() {
        LinkedHashSet<String> categories = new LinkedHashSet<>(DEFAULT_CATEGORIES);
        categories.addAll(feedbackJpaRepository.findDistinctFeedbackCategories().stream()
                .map(this::normalizeCategoryLabel)
                .filter(value -> value != null && !value.isBlank())
                .toList());
        return List.copyOf(categories);
    }

    public AdminFeedbackDetailsResponse getFeedbackDetails(UUID feedbackId) {
        feedbackJpaRepository.incrementViewCount(feedbackId);
        AdminFeedbackDashboardRow row = feedbackJpaRepository.findAdminFeedbackRowById(feedbackId)
                .orElseThrow(() -> new NotFoundException("Feedback", feedbackId));

        AdminFeedbackCardItem item = mapRow(row);
        return new AdminFeedbackDetailsResponse(
                item,
                true,
                true,
                getStatusOptions()
        );
    }

    @Transactional
    public UUID createFeedback(
            UUID authorId,
            String title,
            String content,
            FeedbackType type,
            Boolean isPublic,
            String category,
            String location,
            Boolean isAnonymous,
            String imageUrl
    ) {
            boolean effectiveIsPublic = isPublic == null || isPublic;
            FeedbackType effectiveType = type != null
                ? type
                : (effectiveIsPublic ? FeedbackType.SUGGESTION : FeedbackType.COMPLAINT);

        return submitFeedbackHandler.handle(new SubmitFeedbackCommand(
                authorId,
                title,
                content,
                effectiveType,
                effectiveIsPublic,
                normalizeCategoryLabel(category),
                location,
                Boolean.TRUE.equals(isAnonymous),
                imageUrl
        ));
    }

    @Transactional
    public void respondToFeedback(UUID feedbackId, String response, AdminFeedbackStatusFilter newStatus) {
        if (response == null || response.isBlank()) {
            throw new BusinessRuleException("response is required");
        }

        AdminFeedbackDashboardRow existing = feedbackJpaRepository.findAdminFeedbackRowById(feedbackId)
                .orElseThrow(() -> new NotFoundException("Feedback", feedbackId));

        int updatedRows = feedbackJpaRepository.updateAdminResponse(feedbackId, response.trim());
        if (updatedRows == 0) {
            throw new NotFoundException("Feedback", feedbackId);
        }

        if (newStatus != null && newStatus != AdminFeedbackStatusFilter.ALL) {
            updateFeedbackStatus(feedbackId, newStatus);
        } else if (toUiStatus(existing.getWorkflowStatus()) == AdminFeedbackStatusFilter.PENDING) {
            updateFeedbackStatus(feedbackId, AdminFeedbackStatusFilter.UNDER_REVIEW);
        }
    }

    @Transactional
    public void updateFeedbackStatus(UUID feedbackId, AdminFeedbackStatusFilter newStatus) {
        if (newStatus == null || newStatus == AdminFeedbackStatusFilter.ALL) {
            throw new BusinessRuleException("newStatus must be one of PENDING, UNDER_REVIEW, RESOLVED, ARCHIVED");
        }

        feedbackJpaRepository.findById(feedbackId)
                .orElseThrow(() -> new NotFoundException("Feedback", feedbackId));

        updateFeedbackStatusHandler.handle(new UpdateFeedbackStatusCommand(
                feedbackId,
                toDomainStatus(newStatus)
        ));
    }

    @Transactional
    public void deleteFeedback(UUID feedbackId) {
        if (!feedbackJpaRepository.existsById(feedbackId)) {
            throw new NotFoundException("Feedback", feedbackId);
        }

        feedbackJpaRepository.deleteById(feedbackId);
    }

    private AdminFeedbackSummary mapSummary(AdminFeedbackSummaryRow row) {
        if (row == null) {
            return new AdminFeedbackSummary(0, 0, 0, 0, 0, 0, POPULAR_THRESHOLD);
        }

        return new AdminFeedbackSummary(
                safeLong(row.getPublicSuggestionsCount()),
                safeLong(row.getPrivateFeedbackCount()),
                safeLong(row.getTotalSuggestions()),
                safeLong(row.getPendingReviewCount()),
                safeLong(row.getTotalVotes()),
                safeLong(row.getPopularCount()),
                POPULAR_THRESHOLD
        );
    }

    private AdminFeedbackCardItem mapRow(AdminFeedbackDashboardRow row) {
        int upvotes = row.getUpvotes() == null ? 0 : row.getUpvotes();
        int downvotes = row.getDownvotes() == null ? 0 : row.getDownvotes();
        int voteCount = Math.max(0, upvotes + downvotes);
        boolean isAnonymous = Boolean.TRUE.equals(row.getIsAnonymous());

        AdminFeedbackStatusFilter uiStatusFilter = toUiStatus(row.getWorkflowStatus());

        return new AdminFeedbackCardItem(
                row.getFeedbackId(),
                row.getAuthorId(),
                resolveAuthorName(row.getAuthorFirstName(), row.getAuthorLastName(), isAnonymous),
                row.getUnitNumber(),
                row.getTitle(),
                row.getContent(),
                row.getType(),
                Boolean.TRUE.equals(row.getIsPublic()),
                isAnonymous,
                uiStatusFilter.name(),
                toUiStatusLabel(uiStatusFilter),
                row.getWorkflowStatus(),
                upvotes,
                downvotes,
                voteCount,
                normalizeCategoryLabel(row.getCategory()),
                row.getLocation(),
                row.getAdminResponse(),
                row.getImageUrl(),
                row.getViewCount() == null ? 0 : row.getViewCount(),
                row.getCreatedAt()
        );
    }

    private String resolveAuthorName(String firstName, String lastName, boolean isAnonymous) {
        if (isAnonymous) {
            return "Anonymous Resident";
        }

        String composedName = (safe(firstName) + " " + safe(lastName)).trim();
        return composedName.isBlank() ? "Resident" : composedName;
    }

    private Boolean resolvePublicFilter(AdminFeedbackTab tab) {
        return switch (tab) {
            case PUBLIC_SUGGESTIONS -> Boolean.TRUE;
            case PRIVATE_FEEDBACK -> Boolean.FALSE;
        };
    }

    private String resolveTypeFilter(AdminFeedbackTab tab) {
        return switch (tab) {
            case PUBLIC_SUGGESTIONS -> FeedbackType.SUGGESTION.name();
            case PRIVATE_FEEDBACK -> null;
        };
    }

    private boolean matchesSearch(AdminFeedbackCardItem item, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }

        String needle = search.trim().toLowerCase(Locale.ROOT);
        String haystack = String.join(" ",
                safe(item.title()),
                safe(item.content()),
                safe(item.authorName()),
                safe(item.unitNumber()),
                safe(item.category())
        ).toLowerCase(Locale.ROOT);

        return haystack.contains(needle);
    }

    private boolean matchesStatus(AdminFeedbackCardItem item, AdminFeedbackStatusFilter status) {
        if (status == null || status == AdminFeedbackStatusFilter.ALL) {
            return true;
        }

        return item.uiStatus().equals(status.name());
    }

    private boolean matchesCategory(AdminFeedbackCardItem item, String category) {
        if (category == null || category.isBlank()) {
            return true;
        }

        String normalizedFilter = normalizeCategoryLabel(category);
        if (normalizedFilter == null || normalizedFilter.equalsIgnoreCase("All Categories")) {
            return true;
        }

        return normalizedFilter.equalsIgnoreCase(normalizeCategoryLabel(item.category()));
    }

    private AdminFeedbackStatusFilter toUiStatus(String workflowStatus) {
        if (workflowStatus == null || workflowStatus.isBlank()) {
            return AdminFeedbackStatusFilter.PENDING;
        }

        return switch (workflowStatus.trim().toUpperCase(Locale.ROOT)) {
            case "OPEN" -> AdminFeedbackStatusFilter.PENDING;
            case "UNDER_REVIEW" -> AdminFeedbackStatusFilter.UNDER_REVIEW;
            case "ADDRESSED", "APPROVED" -> AdminFeedbackStatusFilter.RESOLVED;
            case "CLOSED" -> AdminFeedbackStatusFilter.ARCHIVED;
            default -> AdminFeedbackStatusFilter.PENDING;
        };
    }

    private FeedbackStatus toDomainStatus(AdminFeedbackStatusFilter statusFilter) {
        return switch (statusFilter) {
            case PENDING -> FeedbackStatus.OPEN;
            case UNDER_REVIEW -> FeedbackStatus.UNDER_REVIEW;
            case RESOLVED -> FeedbackStatus.ADDRESSED;
            case ARCHIVED -> FeedbackStatus.CLOSED;
            case ALL -> throw new BusinessRuleException("ALL is not a valid writable status");
        };
    }

    private String toUiStatusLabel(AdminFeedbackStatusFilter statusFilter) {
        return switch (statusFilter) {
            case PENDING -> "Pending";
            case UNDER_REVIEW -> "Under Review";
            case RESOLVED -> "Resolved";
            case ARCHIVED -> "Archived";
            case ALL -> "All Status";
        };
    }

    private String normalizeCategoryLabel(String rawCategory) {
        if (rawCategory == null || rawCategory.isBlank()) {
            return "Other";
        }

        String normalized = rawCategory.trim().toUpperCase(Locale.ROOT)
                .replace("&", " AND ")
                .replace('_', ' ')
                .replace('-', ' ')
                .replaceAll("\\s+", " ");

        if (normalized.contains("FACIL")) {
            return "Facilities";
        }
        if (normalized.contains("MAINT")) {
            return "Maintenance";
        }
        if (normalized.contains("SECUR")) {
            return "Security";
        }
        if (normalized.contains("EVENT") || normalized.contains("ACTIVIT")) {
            return "Events & Activities";
        }
        if (normalized.contains("SERVICE")) {
            return "Services";
        }
        if (normalized.contains("OTHER")) {
            return "Other";
        }

        return toDisplayCase(rawCategory.trim());
    }

    private String toDisplayCase(String value) {
        String[] words = value.toLowerCase(Locale.ROOT).split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (words[i].isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(words[i].charAt(0)));
            if (words[i].length() > 1) {
                builder.append(words[i].substring(1));
            }
        }
        return builder.toString();
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    public record AdminFeedbackDashboardResponse(
            List<AdminFeedbackCardItem> items,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext,
            boolean hasPrevious,
            AdminFeedbackSummary summary,
            AdminFeedbackTabCounters tabs
    ) {
    }

    public record AdminFeedbackSummary(
            long publicSuggestionsCount,
            long privateFeedbackCount,
            long totalSuggestions,
            long pendingReviewCount,
            long totalVotes,
            long popularCount,
            int popularThreshold
    ) {
    }

    public record AdminFeedbackTabCounters(
            long publicSuggestions,
            long privateFeedback
    ) {
    }

    public record AdminFeedbackCardItem(
            UUID feedbackId,
            UUID authorId,
            String authorName,
            String unitNumber,
            String title,
            String content,
            String type,
            boolean isPublic,
            boolean isAnonymous,
            String uiStatus,
            String uiStatusLabel,
            String workflowStatus,
            int upvotes,
            int downvotes,
            int voteCount,
            String category,
            String location,
            String adminResponse,
            String imageUrl,
            int viewCount,
            Instant createdAt
    ) {
    }

    public record AdminFeedbackDetailsResponse(
            AdminFeedbackCardItem feedback,
            boolean canRespond,
            boolean canDelete,
            List<String> statusOptions
    ) {
    }
}
