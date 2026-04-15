package com.theMs.sakany.events.internal.application.queries;

import com.theMs.sakany.shared.cqrs.Query;
import java.util.UUID;

public record GetEventDetailsQuery(UUID eventId) implements Query<EventDto> {}
