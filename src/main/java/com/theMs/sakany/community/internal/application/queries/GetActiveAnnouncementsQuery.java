package com.theMs.sakany.community.internal.application.queries;

import com.theMs.sakany.community.internal.domain.Announcement;
import com.theMs.sakany.shared.cqrs.Query;

import java.util.List;

public record GetActiveAnnouncementsQuery() implements Query<List<Announcement>> {}
