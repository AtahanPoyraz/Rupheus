package ai.rupheus.application.service;

import ai.rupheus.application.component.CryptoUtil;
import ai.rupheus.application.dto.admin.CreateTargetRequest;
import ai.rupheus.application.dto.admin.CreateUserRequest;
import ai.rupheus.application.dto.admin.UpdateTargetRequest;
import ai.rupheus.application.dto.admin.UpdateUserRequest;
import ai.rupheus.application.model.TargetModel;
import ai.rupheus.application.model.UserModel;
import ai.rupheus.application.model.enums.ConnectionScheme;
import ai.rupheus.application.repository.TargetRepository;
import ai.rupheus.application.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.validation.Validator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final TargetRepository targetRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final CryptoUtil cryptoUtil;

    @Autowired
    public AdminService(
            UserRepository userRepository,
            TargetRepository targetRepository,
            PasswordEncoder passwordEncoder,
            ObjectMapper objectMapper,
            Validator validator,
            CryptoUtil cryptoUtil
    ) {
        this.userRepository = userRepository;
        this.targetRepository = targetRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.cryptoUtil = cryptoUtil;
    }

    public UserModel getUserByUserId(UUID userId) {
        return this.userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId.toString()));
    }

    public UserModel getUserByEmail(String email) {
        return this.userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }

    public Page<UserModel> getAllUsers(Pageable pageable) {
        return this.userRepository.findAll(pageable);
    }

    @Transactional
    public UserModel createUser(CreateUserRequest createUserRequest) {
        this.userRepository.findByEmail(createUserRequest.getEmail())
                .ifPresent(_ -> {throw new IllegalStateException("User already exists with email: " + createUserRequest.getEmail());});

        UserModel createdUser = new UserModel();
        createdUser.setFirstName(createUserRequest.getFirstName());
        createdUser.setLastName(createUserRequest.getLastName());
        createdUser.setEmail(createUserRequest.getEmail());
        createdUser.setPassword(this.passwordEncoder.encode(createUserRequest.getPassword()));
        createdUser.setIsEnabled(createUserRequest.getIsEnabled());
        createdUser.setIsAccountNonExpired(createUserRequest.getIsAccountNonExpired());
        createdUser.setIsAccountNonLocked(createUserRequest.getIsAccountNonLocked());
        createdUser.setIsCredentialsNonExpired(createUserRequest.getIsCredentialsNonExpired());
        createdUser.setRoles(createUserRequest.getRoles());

        return this.userRepository.save(createdUser);
    }

    @Transactional
    public UserModel updateUserByUserId(UUID userId, UpdateUserRequest updateUserRequest) {
        UserModel updatedUser = this.userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (updateUserRequest.getFirstName() != null && !updateUserRequest.getFirstName().isEmpty()) {
            updatedUser.setFirstName(updateUserRequest.getFirstName());
        }

        if (updateUserRequest.getLastName() != null && !updateUserRequest.getLastName().isEmpty()) {
            updatedUser.setLastName(updateUserRequest.getLastName());
        }

        if (updateUserRequest.getEmail() != null && !updateUserRequest.getEmail().isEmpty()) {
            updatedUser.setEmail(updateUserRequest.getEmail());
        }

        if (updateUserRequest.getPassword() != null && !updateUserRequest.getPassword().isEmpty()) {
            updatedUser.setPassword(this.passwordEncoder.encode(updateUserRequest.getPassword()));
        }

        if (updateUserRequest.getIsEnabled() != null) {
            updatedUser.setIsEnabled(updateUserRequest.getIsEnabled());
        }

        if (updateUserRequest.getIsAccountNonExpired() != null) {
            updatedUser.setIsAccountNonExpired(updateUserRequest.getIsAccountNonExpired());
        }

        if (updateUserRequest.getIsAccountNonLocked() != null) {
            updatedUser.setIsAccountNonLocked(updateUserRequest.getIsAccountNonLocked());
        }

        if (updateUserRequest.getIsCredentialsNonExpired() != null) {
            updatedUser.setIsCredentialsNonExpired(updateUserRequest.getIsCredentialsNonExpired());
        }

        if (updateUserRequest.getRoles() != null) {
            updatedUser.setRoles(updateUserRequest.getRoles());
        }

        return this.userRepository.save(updatedUser);
    }

    @Transactional
    public List<UserModel> deleteUserByUserIds(List<UUID> userIds) {

        List<UserModel> users = this.userRepository.findAllById(userIds);

        if (users.isEmpty()) {
            throw new EntityNotFoundException("No users found for provided IDs: " + userIds.toString());
        }

        this.userRepository.deleteAllInBatch(users);

        return users;
    }

    public TargetModel getTargetByTargetId(UUID targetId) {
        return this.targetRepository.findById(targetId)
                .orElseThrow(() -> new EntityNotFoundException("Target not found with id: " + targetId));
    }

    public List<TargetModel> getTargetByUserId(UUID userId) {
        return this.targetRepository.findAllByUser_Id(userId);
    }

    public Page<TargetModel> getAllTargets(Pageable pageable) {
        return this.targetRepository.findAll(pageable);
    }

    @Transactional
    public TargetModel createTarget(ConnectionScheme connectionScheme, CreateTargetRequest createTargetRequest) {
        UserModel user = this.userRepository.findById(createTargetRequest.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + createTargetRequest.getUserId()));

        this.validateConfig(createTargetRequest.getConfig(),connectionScheme.getConfigClass());
        this.encryptField(createTargetRequest.getConfig(), "apiKey");

        TargetModel createdTarget = new TargetModel();
        createdTarget.setName(createTargetRequest.getTargetName());
        createdTarget.setDescription(createTargetRequest.getTargetDescription());
        createdTarget.setScheme(connectionScheme);
        createdTarget.setConfig(createTargetRequest.getConfig());
        createdTarget.setUser(user);

        return this.targetRepository.save(createdTarget);
    }

    @Transactional
    public TargetModel updateTargetByTargetId(UUID targetId, UpdateTargetRequest updateTargetRequest) {
        TargetModel updatedTarget = this.targetRepository.findById(targetId)
                .orElseThrow(() -> new EntityNotFoundException("Target not found with id: " + targetId));

        Object config = this.objectMapper.convertValue(updateTargetRequest.getConfig(), updatedTarget.getConfig().getClass());

        Set<ConstraintViolation<Object>> violations = this.validator.validate(config);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Invalid config: " + violations.iterator().next().getMessage());
        }

        if (updateTargetRequest.getTargetName() != null && !updateTargetRequest.getTargetName().isEmpty()) {
            updatedTarget.setName(updateTargetRequest.getTargetName());
        }

        if (updateTargetRequest.getTargetDescription() != null && !updateTargetRequest.getTargetDescription().isEmpty()) {
            updatedTarget.setDescription(updateTargetRequest.getTargetDescription());
        }

        if (updateTargetRequest.getConfig() != null) {
            this.validateConfig(updateTargetRequest.getConfig(), updatedTarget.getScheme().getConfigClass());
            this.encryptField(updateTargetRequest.getConfig(), "apiKey");

            updatedTarget.setConfig(updateTargetRequest.getConfig());
        }

        return this.targetRepository.save(updatedTarget);
    }

    @Transactional
    public List<TargetModel> deleteTargetByTargetIds(List<UUID> targetIds) {
        List<TargetModel> targets = this.targetRepository.findAllById(targetIds);
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
            config.put(field, this.cryptoUtil.encrypt(config.get(field).toString()));
        }
    }

    private void decryptField(Map<String, Object> config, String field) {
        if (config.containsKey(field) && config.get(field) != null) {
            config.put(field, this.cryptoUtil.decrypt(config.get(field).toString()));
        }
    }
}
