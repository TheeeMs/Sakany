package com.theMs.sakany.events.internal.infrastructure.persistence;

import com.theMs.sakany.events.internal.domain.EventStatus;
import com.theMs.sakany.shared.jpa.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "community_events")
public class CommunityEventEntity extends BaseEntity {
    private UUID organizerId;
    private String title;
    private String description;
    private String location;
    private Instant startDate;
    private Instant endDate;
    private String imageUrl;
    private String hostName;
    private Double price;
    private Integer maxAttendees;
    private String category;
    private String hostRole;
    private String contactPhone;
    private Double latitude;
    private Double longitude;
    private int currentAttendees;

    @Enumerated(EnumType.STRING)
    private EventStatus status;
    private UUID approvedBy;

    // Default constructor for JPA
    protected CommunityEventEntity() {}

    public CommunityEventEntity(UUID id, UUID organizerId, String title, String description, String location,
                                Instant startDate, Instant endDate, String imageUrl, String hostName, Double price, Integer maxAttendees, 
                                String category, String hostRole, String contactPhone, Double latitude, Double longitude, 
                                int currentAttendees, EventStatus status, UUID approvedBy) {
        try {
            java.lang.reflect.Field idField = com.theMs.sakany.shared.jpa.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(this, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    public UUID getOrganizerId() { return organizerId; }
    public void setOrganizerId(UUID organizerId) { this.organizerId = organizerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Instant getStartDate() { return startDate; }
    public void setStartDate(Instant startDate) { this.startDate = startDate; }

    public Instant getEndDate() { return endDate; }
    public void setEndDate(Instant endDate) { this.endDate = endDate; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getMaxAttendees() { return maxAttendees; }
    public void setMaxAttendees(Integer maxAttendees) { this.maxAttendees = maxAttendees; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getHostRole() { return hostRole; }
    public void setHostRole(String hostRole) { this.hostRole = hostRole; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public int getCurrentAttendees() { return currentAttendees; }
    public void setCurrentAttendees(int currentAttendees) { this.currentAttendees = currentAttendees; }

    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }

    public UUID getApprovedBy() { return approvedBy; }
    public void setApprovedBy(UUID approvedBy) { this.approvedBy = approvedBy; }
}
