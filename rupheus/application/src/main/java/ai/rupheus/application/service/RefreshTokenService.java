package ai.rupheus.application.service;

import ai.rupheus.application.model.RefreshTokenModel;
import ai.rupheus.application.model.UserModel;
import ai.rupheus.application.repository.RefreshTokenRepository;
import ai.rupheus.application.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${security.refresh_token.byte_size}")
    private int refreshTokenByteSize;

    @Value("${security.refresh_token.expiration}")
    private long refreshTokenExpiration;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public RefreshTokenService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String generateRefreshToken(UUID userId) {
        UserModel user = this.userRepository.findById(userId)
                .orElseThrow(
                        () -> new EntityNotFoundException("User not found with id: " + userId)
                );

        this.revokeAllTokensByUserId(userId);

        String rawToken = generateSecureToken(this.refreshTokenByteSize);
        String hash = this.passwordEncoder.encode(rawToken);

        RefreshTokenModel token = new RefreshTokenModel();
        token.setUser(user);
        token.setTokenHash(hash);
        token.setIsRevoked(false);
        token.setExpiresAt(LocalDateTime.now().plusSeconds(this.refreshTokenExpiration / 1000));

        this.refreshTokenRepository.save(token);

        return rawToken;
    }

    public Optional<RefreshTokenModel> findValidToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }

        return this.refreshTokenRepository.findAll().stream()
                .filter(t -> !t.getIsRevoked())
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
                .filter(t -> this.passwordEncoder.matches(rawToken, t.getTokenHash()))
                .findFirst();
    }

    @Transactional
    public void revokeAllTokensByUserId(UUID userId) {
        List<RefreshTokenModel> tokens =
                this.refreshTokenRepository.findAllByUserIdAndIsRevokedFalse(userId);

        tokens.forEach(t -> t.setIsRevoked(true));
        this.refreshTokenRepository.saveAll(tokens);
    }

    @Transactional
    public void revokeToken(String rawToken) {
        List<RefreshTokenModel> tokens = this.refreshTokenRepository.findAll();

        tokens.stream()
                .filter(t -> this.passwordEncoder.matches(rawToken, t.getTokenHash()))
                .forEach(t -> {
                    t.setIsRevoked(true);
                    this.refreshTokenRepository.save(t);
                });
    }

    @Transactional
    public int cleanExpiredTokens() {
        List<RefreshTokenModel> expired = this.refreshTokenRepository.findAllByExpiresAtBefore(LocalDateTime.now());
        this.refreshTokenRepository.deleteAll(expired);

        return expired.size();
    }

    private String generateSecureToken(int byteSize) {
        byte[] bytes = new byte[byteSize];
        new SecureRandom().nextBytes(bytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
