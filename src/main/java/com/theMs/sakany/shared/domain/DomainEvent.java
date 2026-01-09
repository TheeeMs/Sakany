package com.theMs.sakany.shared.domain;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredAt();
}
