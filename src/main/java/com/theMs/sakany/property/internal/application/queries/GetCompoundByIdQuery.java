package com.theMs.sakany.property.internal.application.queries;

import com.theMs.sakany.property.internal.domain.Compound;
import com.theMs.sakany.shared.cqrs.Query;
import java.util.UUID;

public record GetCompoundByIdQuery(
    UUID id
) implements Query<Compound> {
}
