package com.theMs.sakany.accounts.internal.application.commands;

import com.theMs.sakany.accounts.internal.domain.ResidentProfile;
import com.theMs.sakany.accounts.internal.domain.User;
import com.theMs.sakany.accounts.internal.domain.UserRepository;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class CreateUserCommandHandler implements CommandHandler<CreateUserCommand, UUID> {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public CreateUserCommandHandler(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UUID handle(CreateUserCommand command) {
        String hashedPassword = command.password() != null ? passwordEncoder.encode(command.password()) : null;

        User user = User.create(
            command.firstName(),
            command.lastName(),
            command.phoneNumber(),
            command.email(),
            hashedPassword,
            command.loginMethod()
        );

        if (command.unitId() != null && command.type() != null) {
            ResidentProfile residentProfile = ResidentProfile.create(
                user.getId(),
                command.unitId(),
                LocalDate.now(),
                command.type()
            );
            user.setResidentProfile(residentProfile);
        }

        User savedUser = userRepository.save(user);

        return savedUser.getId();
    }
}

