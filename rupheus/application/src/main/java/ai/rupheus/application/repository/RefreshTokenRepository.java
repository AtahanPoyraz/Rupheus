package ai.rupheus.application.repository;

import ai.rupheus.application.model.RefreshTokenModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenModel, UUID> {
    List<RefreshTokenModel> findAllByUserIdAndIsRevokedFalse(UUID userId);
    List<RefreshTokenModel> findAllByExpiresAtBefore(LocalDateTime now);
}
