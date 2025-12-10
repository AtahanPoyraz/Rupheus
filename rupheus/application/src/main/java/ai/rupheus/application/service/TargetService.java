package ai.rupheus.application.service;

import ai.rupheus.application.infrastructure.crypto.CryptoManager;
import ai.rupheus.application.dto.target.CreateTargetRequest;
import ai.rupheus.application.dto.target.UpdateTargetRequest;
import ai.rupheus.application.infrastructure.llm.provider.LLMProvider;
import ai.rupheus.application.infrastructure.llm.provider.LLMProviderResolver;
import ai.rupheus.application.infrastructure.validator.ObjectValidator;
import ai.rupheus.application.model.target.TargetModel;
import ai.rupheus.application.model.user.UserModel;
import ai.rupheus.application.model.target.ConnectionScheme;
import ai.rupheus.application.repository.TargetRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TargetService {
    private final TargetRepository targetRepository;
    private final ObjectMapper objectMapper;
    private final ObjectValidator objectValidator;
    private final CryptoManager cryptoManager;
    private final LLMProviderResolver llmProviderResolver;

    @Autowired
    public TargetService(
            TargetRepository targetRepository,
            ObjectMapper objectMapper,
            ObjectValidator objectValidator,
            CryptoManager cryptoManager,
            LLMProviderResolver llmProviderResolver
    ) {
        this.targetRepository = targetRepository;
        this.objectMapper = objectMapper;
        this.objectValidator = objectValidator;
        this.cryptoManager = cryptoManager;
        this.llmProviderResolver = llmProviderResolver;
    }

    public TargetModel getTargetByTargetId(UUID userId, UUID targetId) {
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
        createdTarget.setConnectionScheme(connectionScheme);

        Object configObject = this.objectMapper
                .convertValue(createTargetRequest.getConfig(), connectionScheme.getConfigClass());

        this.objectValidator.validate(configObject);

        LLMProvider llmProvider = this.llmProviderResolver.resolve(connectionScheme);
        if (!llmProvider.testConnection(configObject)) {
            throw new IllegalStateException("Connection failed");
        }

        this.cryptoManager.encryptField(createTargetRequest.getConfig(), "apiKey");

        createdTarget.setConfig(createTargetRequest.getConfig());
        createdTarget.setUser(user);

        return targetRepository.save(createdTarget);
    }

    @Transactional
    public TargetModel updateTargetByTargetId(UUID userId, UUID targetId, UpdateTargetRequest updateTargetRequest) {
        TargetModel updatedTarget = this.targetRepository.findByUser_IdAndId(userId, targetId)
                .orElseThrow(() -> new EntityNotFoundException("Target not found with id: " + targetId));

        if (updateTargetRequest.getTargetName() != null && !updateTargetRequest.getTargetName().isEmpty()) {
            updatedTarget.setName(updateTargetRequest.getTargetName());
        }

        if (updateTargetRequest.getTargetDescription() != null && !updateTargetRequest.getTargetDescription().isEmpty()) {
            updatedTarget.setDescription(updateTargetRequest.getTargetDescription());
        }

        if (updateTargetRequest.getConfig() != null) {
            Object configObject = this.objectMapper
                    .convertValue(updateTargetRequest.getConfig(), updatedTarget.getConnectionScheme().getConfigClass());

            this.objectValidator.validate(configObject);

            LLMProvider llmProvider = this.llmProviderResolver.resolve(updatedTarget.getConnectionScheme());
            if (!llmProvider.testConnection(configObject)) {
                throw new IllegalStateException("Connection failed");
            }

            this.cryptoManager.encryptField(updateTargetRequest.getConfig(), "apiKey");

            updatedTarget.setConfig(updateTargetRequest.getConfig());
        }

        return this.targetRepository.save(updatedTarget);
    }

    @Transactional
    public List<TargetModel> deleteTargetByTargetIds(UUID userId, List<UUID> targetIds) {
        List<TargetModel> targets = this.targetRepository.findAllByUser_IdAndIdIn(userId, targetIds);

        if (targets.isEmpty()) {
            throw new EntityNotFoundException("No targets found for provided IDs: " + targetIds);
        }

        this.targetRepository.deleteAllInBatch(targets);

        return targets;
    }
}
