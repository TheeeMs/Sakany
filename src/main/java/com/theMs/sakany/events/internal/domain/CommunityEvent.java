package com.theMs.sakany.events.internal.domain;

import com.theMs.sakany.events.internal.domain.events.EventApproved;
import com.theMs.sakany.events.internal.domain.events.EventCancelled;
import com.theMs.sakany.events.internal.domain.events.EventProposed;
import com.theMs.sakany.events.internal.domain.events.EventRejected;
import com.theMs.sakany.shared.domain.AggregateRoot;
import com.theMs.sakany.shared.exception.BusinessRuleException;

import java.time.Instant;
import java.util.UUID;

public class CommunityEvent extends AggregateRoot {
    private final UUID id;
    private final UUID organizerId;
    private final String title;
    private final String description;
    private final String location;
    private final Instant startDate;
    private final Instant endDate;
    private final String imageUrl;
    private final String hostName;
    private final Double price;
    private final Integer maxAttendees;
    private final String category;
    private final String hostRole;
    private final String contactPhone;
    private final Double latitude;
    private final Double longitude;
    private int currentAttendees;
    private EventStatus status;
    private UUID approvedBy;

    // Constructor for reconstruction from persistence
    public CommunityEvent(UUID id, UUID organizerId, String title, String description, String location,
                   Instant startDate, Instant endDate, String imageUrl, String hostName, Double price, Integer maxAttendees, 
                   String category, String hostRole, String contactPhone, Double latitude, Double longitude, 
                   int currentAttendees, EventStatus status, UUID approvedBy) {
        this.id = id;
        this.organizerId = organizerId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.imageUrl = imageUrl;
        this.hostName = hostName;
        this.price = price;
        this.maxAttendees = maxAttendees;
        this.category = category;
        this.hostRole = hostRole;
        this.contactPhone = contactPhone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.currentAttendees = currentAttendees;
        this.status = status;
        this.approvedBy = approvedBy;
    }

    public static CommunityEvent propose(UUID organizerId, String title, String description, String location,
                                         Instant startDate, Instant endDate, String imageUrl, String hostName, Double price, Integer maxAttendees,
                                         String category, String hostRole, String contactPhone, Double latitude, Double longitude) {
        if (title == null || title.isBlank()) throw new BusinessRuleException("Title is required");
        if (description == null || description.isBlank()) throw new BusinessRuleException("Description is required");
        if (location == null || location.isBlank()) throw new BusinessRuleException("Location is required");
        if (startDate == null || startDate.isBefore(Instant.now())) throw new BusinessRuleException("Start date must be in the future");
        if (endDate != null && endDate.isBefore(startDate)) throw new BusinessRuleException("End date must be after start date");
        if (maxAttendees != null && maxAttendees <= 0) throw new BusinessRuleException("Max attendees must be greater than 0");

        UUID id = UUID.randomUUID();
        CommunityEvent event = new CommunityEvent(id, organizerId, title, description, location, startDate, endDate, imageUrl, hostName, price, maxAttendees, 
                                                  category, hostRole, contactPhone, latitude, longitude, 0, EventStatus.PROPOSED, null);
        event.registerEvent(new EventProposed(id, organizerId, title));
        return event;
    }

    public void approve(UUID adminId) {
        if (this.status != EventStatus.PROPOSED) {
            throw new BusinessRuleException("Only PROPOSED events can be approved");
        }
        this.status = EventStatus.APPROVED;
        this.approvedBy = adminId;
        this.registerEvent(new EventApproved(this.id, adminId));
    }

    public void reject() {
        if (this.status != EventStatus.PROPOSED) {
            throw new BusinessRuleException("Only PROPOSED events can be rejected");
        }
        this.status = EventStatus.REJECTED;
        this.registerEvent(new EventRejected(this.id));
    }

    public void cancel() {
        if (this.status == EventStatus.CANCELLED || this.status == EventStatus.COMPLETED) {
            throw new BusinessRuleException("Cannot cancel an already cancelled or completed event");
        }
        this.status = EventStatus.CANCELLED;
        this.registerEvent(new EventCancelled(this.id));
    }

    public void complete() {
        if (this.status != EventStatus.APPROVED) {
            throw new BusinessRuleException("Only APPROVED events can be marked as completed");
        }
        this.status = EventStatus.COMPLETED;
    }

    public void incrementAttendees() {
        if (this.status != EventStatus.APPROVED) {
            throw new BusinessRuleException("Cannot register for an event that is not approved");
        }
        if (this.maxAttendees != null && this.currentAttendees >= this.maxAttendees) {
            throw new BusinessRuleException("Event has reached its maximum capacity");
        }
        this.currentAttendees++;
    }

    public void decrementAttendees() {
        if (this.currentAttendees > 0) {
            this.currentAttendees--;
        }
    }

    public UUID getId() { return id; }
    public UUID getOrganizerId() { return organizerId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public Instant getStartDate() { return startDate; }
    public Instant getEndDate() { return endDate; }
    public String getImageUrl() { return imageUrl; }
    public String getHostName() { return hostName; }
    public Double getPrice() { return price; }
    public Integer getMaxAttendees() { return maxAttendees; }
    public String getCategory() { return category; }
    public String getHostRole() { return hostRole; }
    public String getContactPhone() { return contactPhone; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public int getCurrentAttendees() { return currentAttendees; }
    public EventStatus getStatus() { return status; }
    public UUID getApprovedBy() { return approvedBy; }
}
