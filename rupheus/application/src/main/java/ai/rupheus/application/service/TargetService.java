package ai.rupheus.application.service;

import ai.rupheus.application.dto.target.CreateTargetRequest;
import ai.rupheus.application.dto.target.UpdateTargetRequest;
import ai.rupheus.application.model.TargetModel;
import ai.rupheus.application.model.UserModel;
import ai.rupheus.application.model.enums.ConnectionScheme;
import ai.rupheus.application.repository.TargetRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class TargetService {
    private final TargetRepository targetRepository;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @Autowired
    public TargetService(
            TargetRepository targetRepository,
            ObjectMapper objectMapper,
            Validator validator
    ) {
        this.targetRepository = targetRepository;
        this.objectMapper = objectMapper;
        this.validator = validator;
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
        Object config = this.objectMapper.convertValue(createTargetRequest.getConfig(), connectionScheme.getConfigClass());

        Set<ConstraintViolation<Object>> violations = this.validator.validate(config);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Invalid config: " + violations.iterator().next().getMessage());
        }

        TargetModel createdTarget = new TargetModel();
        createdTarget.setName(createTargetRequest.getTargetName());
        createdTarget.setDescription(createTargetRequest.getTargetDescription());
        createdTarget.setScheme(connectionScheme);
        createdTarget.setConfig(createTargetRequest.getConfig());
        createdTarget.setUser(user);

        return targetRepository.save(createdTarget);
    }

    @Transactional
    public TargetModel updateTargetByTargetId(UUID userId, UUID targetId, UpdateTargetRequest updateTargetRequest) {
        TargetModel updatedTarget = this.targetRepository.findByUser_IdAndId(userId, targetId)
                .orElseThrow(() -> new EntityNotFoundException("Target not found with id: " + targetId));

        Object config = this.objectMapper.convertValue(updateTargetRequest.getConfig(), updatedTarget.getConfig().getClass());

        Set<ConstraintViolation<Object>> violations = this.validator.validate(config);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Invalid config: " + violations.iterator().next().getMessage());
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
