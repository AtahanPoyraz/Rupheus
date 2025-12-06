package ai.rupheus.application.service;

import ai.rupheus.application.dto.admin.CreateTargetRequest;
import ai.rupheus.application.dto.admin.CreateUserRequest;
import ai.rupheus.application.dto.admin.UpdateTargetRequest;
import ai.rupheus.application.dto.admin.UpdateUserRequest;
import ai.rupheus.application.model.TargetModel;
import ai.rupheus.application.model.UserModel;
import ai.rupheus.application.model.enums.ConnectionScheme;
import ai.rupheus.application.repository.TargetRepository;
import ai.rupheus.application.repository.UserRepository;
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

    @Autowired
    public AdminService(
            UserRepository userRepository,
            TargetRepository targetRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.targetRepository = targetRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserModel getUserById(UUID userId) {
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
    public UserModel updateUserByUserId(UUID userId, UpdateUserRequest updateUserByIdRequest) {
        UserModel updatedUser = this.userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (updateUserByIdRequest.getFirstName() != null) {
            updatedUser.setFirstName(updateUserByIdRequest.getFirstName());
        }

        if (updateUserByIdRequest.getLastName() != null) {
            updatedUser.setLastName(updateUserByIdRequest.getLastName());
        }

        if (updateUserByIdRequest.getEmail() != null) {
            updatedUser.setEmail(updateUserByIdRequest.getEmail());
        }

        if (updateUserByIdRequest.getPassword() != null) {
            updatedUser.setPassword(passwordEncoder.encode(updateUserByIdRequest.getPassword()));
        }

        if (updateUserByIdRequest.getIsEnabled() != null) {
            updatedUser.setIsEnabled(updateUserByIdRequest.getIsEnabled());
        }

        if (updateUserByIdRequest.getIsAccountNonExpired() != null) {
            updatedUser.setIsAccountNonExpired(updateUserByIdRequest.getIsAccountNonExpired());
        }

        if (updateUserByIdRequest.getIsAccountNonLocked() != null) {
            updatedUser.setIsAccountNonLocked(updateUserByIdRequest.getIsAccountNonLocked());
        }

        if (updateUserByIdRequest.getIsCredentialsNonExpired() != null) {
            updatedUser.setIsCredentialsNonExpired(updateUserByIdRequest.getIsCredentialsNonExpired());
        }

        if (updateUserByIdRequest.getRoles() != null) {
            updatedUser.setRoles(updateUserByIdRequest.getRoles());
        }

        return this.userRepository.save(updatedUser);
    }

    @Transactional
    public UserModel deleteUserByUserId(UUID userId) {
        UserModel deletedUser = this.userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        this.userRepository.delete(deletedUser);

        return deletedUser;
    }

    @Transactional
    public List<UserModel> bulkDeleteUserByIds(List<UUID> userIds) {

        List<UserModel> users = this.userRepository.findAllById(userIds);

        if (users.isEmpty()) {
            throw new EntityNotFoundException("No users found for provided IDs: " + userIds.toString());
        }

        this.userRepository.deleteAllInBatch(users);

        return users;
    }

    public TargetModel getTargetById(UUID targetId) {
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

        TargetModel createdTarget = new TargetModel();
        createdTarget.setName(createTargetRequest.getTargetName());
        createdTarget.setDescription(createTargetRequest.getTargetDescription());
        createdTarget.setConfig(createTargetRequest.getConfig());
        createdTarget.setScheme(connectionScheme);
        createdTarget.setUser(user);

        return this.targetRepository.save(createdTarget);
    }

    @Transactional
    public TargetModel updateTargetByTargetId(UUID targetId, UpdateTargetRequest updateTargetRequest) {
        TargetModel updatedTarget = this.targetRepository.findById(targetId)
                .orElseThrow(() -> new EntityNotFoundException("Target not found with id: " + targetId));

        if (updateTargetRequest.getTargetName() != null) {
            updatedTarget.setName(updateTargetRequest.getTargetName());
        }

        if (updateTargetRequest.getTargetDescription() != null) {
            updatedTarget.setDescription(updateTargetRequest.getTargetDescription());
        }

        if (updateTargetRequest.getConfig() != null) {
            updatedTarget.setConfig(updateTargetRequest.getConfig());
        }

        return this.targetRepository.save(updatedTarget);
    }

    @Transactional
    public TargetModel deleteTargetById(UUID targetId) {
        TargetModel deletedTarget = this.targetRepository.findById(targetId)
                .orElseThrow(() -> new EntityNotFoundException("Target not found with id: " + targetId));

        this.targetRepository.delete(deletedTarget);

        return deletedTarget;
    }

    @Transactional
    public List<TargetModel> bulkDeleteTargetByIds(List<UUID> targetIds) {
        List<TargetModel> targets = this.targetRepository.findAllById(targetIds);

        if (targets.isEmpty()) {
            throw new EntityNotFoundException("No targets found for provided IDs: " + targetIds);
        }

        this.targetRepository.deleteAllInBatch(targets);

        return targets;
    }
}
