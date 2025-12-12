package ai.rupheus.application.service;

import ai.rupheus.application.common.crypto.CryptoManager;
import ai.rupheus.application.dto.admin.CreateTargetRequest;
import ai.rupheus.application.dto.admin.CreateUserRequest;
import ai.rupheus.application.dto.admin.UpdateTargetRequest;
import ai.rupheus.application.dto.admin.UpdateUserRequest;
import ai.rupheus.application.adapter.llm.provider.LLMProvider;
import ai.rupheus.application.adapter.llm.provider.LLMProviderResolver;
import ai.rupheus.application.common.validator.ObjectValidator;
import ai.rupheus.application.model.target.TargetModel;
import ai.rupheus.application.model.target.TargetStatus;
import ai.rupheus.application.model.user.UserModel;
import ai.rupheus.application.model.target.Provider;
import ai.rupheus.application.repository.TargetRepository;
import ai.rupheus.application.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final TargetRepository targetRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final ObjectValidator objectValidator;
    private final CryptoManager cryptoManager;
    private final LLMProviderResolver llmProviderResolver;

    @Autowired
    public AdminService(
        UserRepository userRepository,
        TargetRepository targetRepository,
        PasswordEncoder passwordEncoder,
        ObjectMapper objectMapper,
        ObjectValidator objectValidator,
        CryptoManager cryptoManager,
        LLMProviderResolver llmProviderResolver
    ) {
        this.userRepository = userRepository;
        this.targetRepository = targetRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
        this.objectValidator = objectValidator;
        this.cryptoManager = cryptoManager;
        this.llmProviderResolver = llmProviderResolver;
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
    public TargetModel createTarget(Provider provider, CreateTargetRequest createTargetRequest) {
        UserModel user = this.userRepository.findById(createTargetRequest.getUserId())
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + createTargetRequest.getUserId()));

        TargetModel createdTarget = new TargetModel();
        createdTarget.setName(createTargetRequest.getTargetName());
        createdTarget.setDescription(createTargetRequest.getTargetDescription());
        createdTarget.setProvider(provider);

        Object configObject = this.objectMapper
            .convertValue(createTargetRequest.getConfig(), provider.getConfigClass());

        this.objectValidator.validate(configObject);

        LLMProvider llmProvider = this.llmProviderResolver.resolve(provider);
        if (llmProvider.testConnection(configObject)) {
            throw new IllegalStateException("Connection failed, Please check your credentials");
        }

        this.cryptoManager.encryptField(createTargetRequest.getConfig(), "apiKey");

        createdTarget.setConfig(createTargetRequest.getConfig());
        createdTarget.setStatus(TargetStatus.CONNECTED);
        createdTarget.setUser(user);

        return this.targetRepository.save(createdTarget);
    }

    @Transactional
    public TargetModel updateTargetByTargetId(UUID targetId, UpdateTargetRequest updateTargetRequest) {
        TargetModel updatedTarget = this.targetRepository.findById(targetId)
            .orElseThrow(() -> new EntityNotFoundException("Target not found with id: " + targetId));

        if (updateTargetRequest.getTargetName() != null && !updateTargetRequest.getTargetName().isEmpty()) {
            updatedTarget.setName(updateTargetRequest.getTargetName());
        }

        if (updateTargetRequest.getTargetDescription() != null && !updateTargetRequest.getTargetDescription().isEmpty()) {
            updatedTarget.setDescription(updateTargetRequest.getTargetDescription());
        }

        if (updateTargetRequest.getConfig() != null) {
            Object configObject = this.objectMapper
                .convertValue(updateTargetRequest.getConfig(), updatedTarget.getProvider().getConfigClass());

            this.objectValidator.validate(configObject);

            LLMProvider llmProvider = this.llmProviderResolver.resolve(updatedTarget.getProvider());
            if (llmProvider.testConnection(configObject)) {
                throw new IllegalStateException("Connection failed, Please check your credentials");
            }

            this.cryptoManager.encryptField(updateTargetRequest.getConfig(), "apiKey");

            updatedTarget.setConfig(updateTargetRequest.getConfig());
        }

        updatedTarget.setStatus(TargetStatus.CONNECTED);

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
}
