package ai.rupheus.application.repository;

import ai.rupheus.application.model.RefreshTokenModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenModel, UUID> {
    Optional<RefreshTokenModel> findByTokenHash(String tokenHash);
    Optional<RefreshTokenModel> findByTokenHashAndIsRevokedFalseAndExpiresAtAfter(
            String tokenHash,
            LocalDateTime now
    );
}
