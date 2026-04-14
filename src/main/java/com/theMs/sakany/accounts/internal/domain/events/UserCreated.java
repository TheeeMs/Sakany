package com.theMs.sakany.accounts.internal.domain.events;

import com.theMs.sakany.accounts.internal.domain.LoginMethod;
import com.theMs.sakany.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record UserCreated(UUID id, String firstName, String lastName, String phoneNumber, LoginMethod loginMethod, Instant occurredAt) implements DomainEvent {
 public UserCreated(UUID id, String firstName, String lastName, String phoneNumber, LoginMethod loginMethod) {
    this(id, firstName, lastName, phoneNumber, loginMethod, Instant.now()); 
 }
}
