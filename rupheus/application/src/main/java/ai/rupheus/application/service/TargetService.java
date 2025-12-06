package ai.rupheus.application.service;

import ai.rupheus.application.config.provider.ProviderUrlResolver;
import ai.rupheus.application.dto.target.CreateTargetRequest;
import ai.rupheus.application.dto.target.UpdateTargetRequest;
import ai.rupheus.application.model.TargetModel;
import ai.rupheus.application.model.UserModel;
import ai.rupheus.application.model.enums.ConnectionScheme;
import ai.rupheus.application.model.pojos.ClaudeConfig;
import ai.rupheus.application.repository.TargetRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TargetService {
    private final TargetRepository targetRepository;
    private final ProviderUrlResolver providerUrlResolver;

    @Autowired
    public TargetService(
            TargetRepository targetRepository,
            ProviderUrlResolver providerUrlResolver
    ) {
        this.targetRepository = targetRepository;
        this.providerUrlResolver = providerUrlResolver;
    }

    public TargetModel getTargetById(UUID userId, UUID targetId) {
        return this.targetRepository.findByUser_IdAndId(userId, targetId)
                .orElseThrow(() -> new EntityNotFoundException("Target not found with id: " + targetId));
    }

    public List<TargetModel> getTargetByUserId(UUID userId) {
        return this.targetRepository.findAllByUser_Id(userId);
    }

    @Transactional
    public TargetModel createTarget(UserModel user, ConnectionScheme connectionScheme, CreateTargetRequest createTargetRequest) {
        TargetModel createdTarget = new TargetModel();
        createdTarget.setName(createTargetRequest.getTargetName());
        createdTarget.setDescription(createTargetRequest.getTargetDescription());
        createdTarget.setScheme(connectionScheme);
        createdTarget.setUser(user);
        return this.targetRepository.save(createdTarget);
    }

    @Transactional
    public TargetModel updateTargetId(UUID userId, UUID targetId, UpdateTargetRequest updateTargetRequest) {
        TargetModel updatedTarget = this.targetRepository.findByUser_IdAndId(userId, targetId)
                .orElseThrow(() -> new EntityNotFoundException("Target not found with id: " + targetId));

        return this.targetRepository.save(updatedTarget);
    }

    @Transactional
    public TargetModel deleteTargetById(UUID userId, UUID targetId) {
        TargetModel deletedTarget = this.targetRepository.findByUser_IdAndId(userId, targetId)
                .orElseThrow(() -> new EntityNotFoundException("Target not found with id: " + targetId));

        this.targetRepository.delete(deletedTarget);

        return deletedTarget;
    }

    @Transactional
    public List<TargetModel> bulkDeleteTargetByIds(UUID userId, List<UUID> targetIds) {
        List<TargetModel> targets = this.targetRepository.findAllByUser_IdAndIdIn(userId, targetIds);

        if (targets.isEmpty()) {
            throw new EntityNotFoundException("No targets found for provided IDs: " + targetIds);
        }

        this.targetRepository.deleteAllInBatch(targets);

        return targets;
    }

    private <T> T matchParameterWithScheme(Object parameters, ConnectionScheme connectionScheme) {
        switch (connectionScheme) {
            case CLAUDE -> new ClaudeConfig();
        }

        throw new IllegalArgumentException("Unknown connection scheme: " + connectionScheme);
    }
}
