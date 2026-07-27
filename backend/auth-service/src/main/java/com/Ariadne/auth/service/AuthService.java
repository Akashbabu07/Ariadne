package com.Ariadne.auth.service;

import com.Ariadne.auth.dto.*;
import com.Ariadne.auth.entity.*;
import com.Ariadne.auth.repository.*;
import com.Ariadne.shared.exception.ConflictException;
import com.Ariadne.shared.exception.ResourceNotFoundException;
import com.Ariadne.shared.exception.UnauthorizedException;
import com.Ariadne.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.findByEmailIgnoreCase(request.email()).isPresent()) {
                     throw new ConflictException("A user with this email already exists");
        }

        User user = User.builder()
                .orgId(request.orgId())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .status(UserStatus.ACTIVE)
                .build();
        user = userRepository.save(user);

        Role engineerRole = roleRepository.findByName("ENGINEER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role ENGINEER not seeded"));

        UserRole userRole = UserRole.builder()
                .user(user)
                .role(engineerRole)
                .orgId(request.orgId())
                .build();
        userRoleRepository.save(userRole);

        return toUserResponse(user, List.of(engineerRole.getName()));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
                User user = userRepository.findByEmailIgnoreCase(request.email())
                               .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account is not active");
        }

        List<String> roles = userRoleRepository.findByUserId(user.getId()).stream()
                .map(ur -> ur.getRole().getName())
                .toList();

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getOrgId(), roles);
        String refreshToken = issueRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken, 3600);
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        String hash = sha256(rawRefreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (stored.getRevokedAt() != null || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token expired or revoked");
        }

        User user = stored.getUser();
        List<String> roles = userRoleRepository.findByUserId(user.getId()).stream()
                .map(ur -> ur.getRole().getName())
                .toList();
        stored.setRevokedAt(Instant.now());
        refreshTokenRepository.save(stored);

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getOrgId(), roles);
        String newRefreshToken = issueRefreshToken(user);

        return new AuthResponse(accessToken, newRefreshToken, 3600);
    }

    private String issueRefreshToken(User user) {
        String raw = UUID.randomUUID().toString() + UUID.randomUUID();
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(sha256(raw))
                .expiresAt(Instant.now().plusMillis(604800000L))
                .build();
        refreshTokenRepository.save(token);
        return raw;
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(digest.digest(value.getBytes()));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private UserResponse toUserResponse(User user, List<String> roles) {
        return new UserResponse(
                user.getId(), user.getOrgId(), user.getEmail(),
                user.getStatus().name(), roles, user.getCreatedAt()
        );
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        String hash = sha256(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
            token.setRevokedAt(Instant.now());
            refreshTokenRepository.save(token);
        });
    }
}