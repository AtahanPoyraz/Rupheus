package ai.rupheus.application.repository;

import ai.rupheus.application.model.TargetModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TargetRepository extends JpaRepository<TargetModel, UUID> {
    List<TargetModel> findAllByUser_Id(UUID userId);
    void deleteAllByIdInBatch(Iterable<UUID> targetIds);
}
