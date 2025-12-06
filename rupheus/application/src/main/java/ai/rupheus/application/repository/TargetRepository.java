package ai.rupheus.application.repository;

import ai.rupheus.application.model.TargetModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TargetRepository extends JpaRepository<TargetModel, UUID> {
    List<TargetModel> findAllByUser_Id(UUID userId);
    List<TargetModel> findAllByUser_IdAndIdIn(UUID userId, List<UUID> targetIds);
    Optional<TargetModel> findByUser_IdAndId(UUID userId, UUID targetId);
    void deleteAllByIdInBatch(Iterable<UUID> targetIds);
}
