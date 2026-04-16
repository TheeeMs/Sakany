package com.theMs.sakany.accounts.internal.application.commands;

import com.theMs.sakany.accounts.internal.domain.LoginMethod;
import com.theMs.sakany.accounts.internal.domain.ResidentType;
import com.theMs.sakany.shared.cqrs.Command;

import java.util.UUID;

public record CreateUserCommand(
    String firstName,
    String lastName,
    String phoneNumber,
    String email,
    String password,
    ResidentType type,
    UUID unitId,
    LoginMethod loginMethod
) implements Command<UUID> {}
