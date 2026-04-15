package com.theMs.sakany.accounts.internal.domain;

import com.theMs.sakany.accounts.internal.domain.events.UserCreated;
import com.theMs.sakany.shared.domain.AggregateRoot;
import com.theMs.sakany.shared.exception.BusinessRuleException;

import java.util.UUID;

public class User extends AggregateRoot {
    private UUID id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String hashedPassword;
    private Role role;
    private boolean isActive;
    private boolean isPhoneVerified;
    private LoginMethod loginMethod;

    private ResidentProfile residentProfile;
    private TechnicianProfile technicianProfile;
    private AdminProfile adminProfile;

    private User(UUID id, String firstName, String lastName, String phoneNumber, String email, String hashedPassword, Role role, boolean isActive, boolean isPhoneVerified, LoginMethod loginMethod) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.role = role;
        this.isActive = isActive;
        this.isPhoneVerified = isPhoneVerified;
        this.loginMethod = loginMethod;
    }

    public static User create(String firstName, String lastName, String phoneNumber, LoginMethod loginMethod) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new BusinessRuleException("Phone number cannot be null or empty");
        }
        UUID id = UUID.randomUUID();
        User user = new User(id, firstName, lastName, phoneNumber, null, null, Role.RESIDENT, true, false, loginMethod);
        user.registerEvent(new UserCreated(id, firstName, lastName, phoneNumber, loginMethod));
        return user;
    }

    public UUID getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isPhoneVerified() {
        return isPhoneVerified;
    }

    public LoginMethod getLoginMethod() {
        return loginMethod;
    }

    public ResidentProfile getResidentProfile() {
        return residentProfile;
    }

    public void setResidentProfile(ResidentProfile residentProfile) {
        this.residentProfile = residentProfile;
    }

    public TechnicianProfile getTechnicianProfile() {
        return technicianProfile;
    }

    public void setTechnicianProfile(TechnicianProfile technicianProfile) {
        this.technicianProfile = technicianProfile;
    }

    public AdminProfile getAdminProfile() {
        return adminProfile;
    }

    public void setAdminProfile(AdminProfile adminProfile) {
        this.adminProfile = adminProfile;
    }
}
