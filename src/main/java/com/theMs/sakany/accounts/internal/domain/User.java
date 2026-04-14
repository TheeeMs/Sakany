package com.theMs.sakany.accounts.internal.domain;

import com.theMs.sakany.accounts.internal.domain.events.UserCreated;
import com.theMs.sakany.shared.domain.AggregateRoot;
import com.theMs.sakany.shared.exception.BusinessRuleException;

import java.util.UUID;

public class User extends AggregateRoot {
    private UUID id;
    // I used the second approach to split the first and last name so what the migration should i do now?
    private String firstName;
    private String lastName;
    // is phone number should be string so can we add +whatever for the country code just normal int?
    private String phoneNumber;
    private String email;
    private String hashedPassword;
    // is role enum path good or should i create spearate dir for the user and its stuff like ROLE and LOGIN METHOD
    private Role role;
    private boolean isActive;
    private boolean isPhoneVerified;

    // why we need the login method
    private LoginMethod loginMethod;

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

//is creating with that factory method is good or deprecated

    public static User create(String firstName, String lastName, String phoneNumber, LoginMethod loginMethod) {


        if (phoneNumber == null || phoneNumber.isEmpty()) {
            // how can i fix that Module 'accounts' depends on non-exposed type 'com.theMs.sakany.shared.exception.BusinessRuleException' from module 'shared'
            throw new BusinessRuleException("Phone number cannot be null or empty");
        }
        UUID id = UUID.randomUUID();
        // we can add some validation here if needed
        User user = new User(id, firstName, lastName, phoneNumber, null, null, Role.RESIDENT, true, false, loginMethod);
        //userCreated event where it should be created first before importing?
        user.registerEvent(new UserCreated(id, firstName, lastName, phoneNumber, loginMethod));
        return user;
    }

}
