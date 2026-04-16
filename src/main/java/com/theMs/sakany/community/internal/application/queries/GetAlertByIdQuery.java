package com.theMs.sakany.community.internal.application.queries;

import com.theMs.sakany.community.internal.domain.Alert;
import com.theMs.sakany.shared.cqrs.Query;

import java.util.Optional;
import java.util.UUID;

public record GetAlertByIdQuery(UUID id) implements Query<Optional<Alert>> {}