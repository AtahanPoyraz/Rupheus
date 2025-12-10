package ai.rupheus.application.service;

import ai.rupheus.application.infrastructure.crypto.CryptoManager;
import ai.rupheus.application.dto.target.CreateTargetRequest;
import ai.rupheus.application.dto.target.UpdateTargetRequest;
import ai.rupheus.application.model.target.TargetModel;
import ai.rupheus.application.model.user.UserModel;
import ai.rupheus.application.model.target.ConnectionScheme;
import ai.rupheus.application.repository.TargetRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.validation.Validator;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class TargetService {
    private final TargetRepository targetRepository;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final CryptoManager cryptoManager;

    @Autowired
    public TargetService(
            TargetRepository targetRepository,
            ObjectMapper objectMapper,
            Validator validator,
            CryptoManager cryptoManager
    ) {
        this.targetRepository = targetRepository;
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.cryptoManager = cryptoManager;
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
        this.validateConfig(createTargetRequest.getConfig(), connectionScheme.getConfigClass());
        this.encryptField(createTargetRequest.getConfig(), "apiKey");

        TargetModel createdTarget = new TargetModel();
        createdTarget.setName(createTargetRequest.getTargetName());
        createdTarget.setDescription(createTargetRequest.getTargetDescription());
        createdTarget.setConnectionScheme(connectionScheme);
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
            this.validateConfig(updateTargetRequest.getConfig(), updatedTarget.getConnectionScheme().getConfigClass());
            this.encryptField(updateTargetRequest.getConfig(), "apiKey");

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

    private void validateConfig(Object config, Class<?> target) {
        Object configObject = this.objectMapper.convertValue(config, target);
        Set<ConstraintViolation<Object>> violations = validator.validate(configObject);

        if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Invalid config: " + violations.iterator().next().getMessage());
        }
    }

    private void encryptField(Map<String, Object> config, String field) {
        if (config.containsKey(field) && config.get(field) != null) {
            config.put(field, this.cryptoManager.encrypt(config.get(field).toString()));
        }
    }

    private void decryptField(Map<String, Object> config, String field) {
        if (config.containsKey(field) && config.get(field) != null) {
            config.put(field, this.cryptoManager.decrypt(config.get(field).toString()));
        }
    }
}
