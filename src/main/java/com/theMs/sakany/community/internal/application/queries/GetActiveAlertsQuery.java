package com.theMs.sakany.community.internal.application.queries;

import com.theMs.sakany.community.internal.domain.Alert;
import com.theMs.sakany.shared.cqrs.Query;

import java.util.List;

public record GetActiveAlertsQuery() implements Query<List<Alert>> {}
