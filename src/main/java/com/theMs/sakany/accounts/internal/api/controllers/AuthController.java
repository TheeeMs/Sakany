package com.theMs.sakany.accounts.internal.api.controllers;

import com.theMs.sakany.accounts.internal.api.dtos.AuthResponse;
import com.theMs.sakany.accounts.internal.api.dtos.LoginEmailRequest;
import com.theMs.sakany.accounts.internal.api.dtos.LoginPhoneRequest;
import com.theMs.sakany.accounts.internal.api.dtos.RefreshRequest;
import com.theMs.sakany.accounts.internal.api.dtos.RegisterRequest;
import com.theMs.sakany.accounts.internal.application.commands.CreateUserCommand;
import com.theMs.sakany.accounts.internal.application.commands.CreateUserCommandHandler;
import com.theMs.sakany.accounts.internal.domain.User;
import com.theMs.sakany.accounts.internal.domain.UserRepository;
import com.theMs.sakany.shared.auth.AccessTokenPayload;
import com.theMs.sakany.shared.auth.JwtService;
import com.theMs.sakany.shared.exception.BusinessRuleException;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final CreateUserCommandHandler createUserCommandHandler;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final com.theMs.sakany.shared.firebase.OtpService otpService;
    private final boolean includeOtpInResponse;

    public AuthController(
            CreateUserCommandHandler createUserCommandHandler,
            UserRepository userRepository,
            JwtService jwtService,
            BCryptPasswordEncoder passwordEncoder,
            com.theMs.sakany.shared.firebase.OtpService otpService,
            @Value("${auth.otp.include-in-response:false}") boolean includeOtpInResponse) {
        this.createUserCommandHandler = createUserCommandHandler;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.includeOtpInResponse = includeOtpInResponse;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        if (request.loginMethod() == null) {
            throw new BusinessRuleException("loginMethod is required");
        }

        UUID userId = createUserCommandHandler.handle(new CreateUserCommand(
                request.firstName(),
                request.lastName(),
                request.phoneNumber(),
                request.email(),
                request.password(),
                request.type(),
                request.unitId(),
                request.loginMethod()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(user.getId(), accessToken, refreshToken));
    }

    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, String>> sendOtp(@RequestBody Map<String, String> request) {
        String phoneNumber = request.get("phoneNumber");
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new BusinessRuleException("phoneNumber is required");
        }

        // Check if user exists
        userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NotFoundException("User", phoneNumber));

        String otp = otpService.generateOtp(phoneNumber);
        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", "OTP sent successfully");
        response.put("phoneNumber", phoneNumber);
        if (includeOtpInResponse) {
            response.put("otpCode", otp);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/phone")
    public ResponseEntity<AuthResponse> loginWithPhone(@RequestBody LoginPhoneRequest request) {
        if (request.otpCode() == null || !request.otpCode().matches("^\\d{6}$")) {
            throw new BusinessRuleException("Invalid OTP format. OTP must be 6 digits");
        }

        User user = userRepository.findByPhoneNumber(request.phoneNumber())
                .orElseThrow(() -> new NotFoundException("User", request.phoneNumber()));

        if (!user.isActive()) {
            throw new BusinessRuleException("User account is inactive");
        }

        // Verify OTP
        if (!otpService.verifyOtp(request.phoneNumber(), request.otpCode())) {
            throw new BusinessRuleException("Invalid or expired OTP");
        }

        if (!user.isPhoneVerified()) {
            user.verifyPhone();
            userRepository.save(user);
        }

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        return ResponseEntity.ok(new AuthResponse(null, accessToken, refreshToken));
    }

    @PostMapping("/login/email")
    public ResponseEntity<AuthResponse> loginWithEmail(@RequestBody LoginEmailRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new NotFoundException("User", request.email()));

        if (!user.isActive()) {
            throw new BusinessRuleException("User account is inactive");
        }

        if (user.getHashedPassword() == null
                || !passwordEncoder.matches(request.password(), user.getHashedPassword())) {
            throw new BusinessRuleException("Invalid email or password");
        }

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        return ResponseEntity.ok(new AuthResponse(null, accessToken, refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
        if (request.refreshToken() == null || request.refreshToken().isBlank()) {
            throw new BusinessRuleException("refreshToken is required");
        }
        if (!jwtService.isRefreshToken(request.refreshToken())) {
            throw new BusinessRuleException("Provided token is not a refresh token");
        }

        AccessTokenPayload payload = jwtService.validateToken(request.refreshToken());
        User user = userRepository.findById(payload.userId())
                .orElseThrow(() -> new NotFoundException("User", payload.userId()));

        if (!user.isActive()) {
            throw new BusinessRuleException("User account is inactive");
        }

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        return ResponseEntity.ok(new AuthResponse(null, accessToken, refreshToken));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessRuleException("No authenticated user");
        }

        UUID userId;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UUID uuid) {
            userId = uuid;
        } else {
            try {
                userId = UUID.fromString(principal.toString());
            } catch (IllegalArgumentException e) {
                throw new BusinessRuleException("Invalid authenticated principal");
            }
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", user.getId());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("phoneNumber", user.getPhoneNumber());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());
        response.put("isActive", user.isActive());
        response.put("isPhoneVerified", user.isPhoneVerified());
        response.put("loginMethod", user.getLoginMethod());

        return ResponseEntity.ok(response);
    }
}
