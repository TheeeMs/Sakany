package com.theMs.sakany.accounts.internal.application.commands;

import com.theMs.sakany.accounts.internal.domain.User;
import com.theMs.sakany.accounts.internal.domain.UserRepository;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CreateUserCommandHandler implements CommandHandler<CreateUserCommand, UUID> {

    private final UserRepository userRepository;

    public CreateUserCommandHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UUID handle(CreateUserCommand command) {
        User user = User.create(
            command.firstName(),
            command.lastName(),
            command.phoneNumber(),
            command.loginMethod()
        );

        User savedUser = userRepository.save(user);

        return savedUser.getId();
    }
}
