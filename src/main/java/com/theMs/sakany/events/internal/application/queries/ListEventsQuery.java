package com.theMs.sakany.events.internal.application.queries;

import com.theMs.sakany.events.internal.domain.EventStatus;
import com.theMs.sakany.shared.cqrs.Query;
import java.util.List;

public record ListEventsQuery(EventStatus status) implements Query<List<EventDto>> {}
