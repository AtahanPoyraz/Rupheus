package ai.rupheus.application.service;

import ai.rupheus.application.model.RefreshTokenModel;
import ai.rupheus.application.model.UserModel;
import ai.rupheus.application.repository.RefreshTokenRepository;
import ai.rupheus.application.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${security.refresh_token.byte_size}")
    private int refreshTokenByteSize;

    @Value("${security.refresh_token.expiration}")
    private long refreshTokenExpiration;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public String generateRefreshToken(UUID userId) {
        UserModel user = this.userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        String rawToken = this.generateSecureToken(this.refreshTokenByteSize);

        RefreshTokenModel token = new RefreshTokenModel();
        token.setUser(user);
        token.setTokenHash(DigestUtils.sha256Hex(rawToken));
        token.setIsRevoked(false);
        token.setExpiresAt(LocalDateTime.now().plusSeconds(this.refreshTokenExpiration / 1000));

        this.refreshTokenRepository.save(token);

        return rawToken;
    }

    public RefreshTokenModel findValidToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new EntityNotFoundException("Refresh token not found");
        }

        return this.refreshTokenRepository
                .findByTokenHashAndIsRevokedFalseAndExpiresAtAfter(
                        DigestUtils.sha256Hex(rawToken),
                        LocalDateTime.now()
                )
                .orElseThrow(
                        () -> new EntityNotFoundException("Refresh token not found")
                );
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
        this.refreshTokenRepository
                .findByTokenHash(DigestUtils.sha256Hex(rawToken))
                .ifPresent(t -> {
                    t.setIsRevoked(true);
                    this.refreshTokenRepository.save(t);
                });
    }

    @Transactional
    public int cleanExpiredTokens() {
        List<RefreshTokenModel> expired =
                this.refreshTokenRepository.findAllByExpiresAtBefore(LocalDateTime.now());

        this.refreshTokenRepository.deleteAll(expired);

        return expired.size();
    }

    private String generateSecureToken(int byteSize) {
        byte[] bytes = new byte[byteSize];
        new SecureRandom().nextBytes(bytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
