package ai.rupheus.application.service;

import ai.rupheus.application.dto.user.CreateUserRequest;
import ai.rupheus.application.dto.user.UpdateUserByIdRequest;
import ai.rupheus.application.model.UserModel;
import ai.rupheus.application.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserModel getUserById(UUID userId) {
        return this.userRepository.findById(userId)
                .orElseThrow(
                        () -> new EntityNotFoundException("User not found with id: " + userId.toString())
                );
    }

    public UserModel getUserByEmail(String email) {
        return this.userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new EntityNotFoundException("User not found with email: " + email)
                );
    }

    public Page<UserModel> getAllUsers(Pageable pageable) {
        return this.userRepository.findAll(pageable);
    }

    @Transactional
    public UserModel createUser(CreateUserRequest request) {
        this.userRepository.findByEmail(request.getEmail())
                .ifPresent(_ -> {
                    throw new IllegalStateException("User already exists with email: " + request.getEmail());
                }
        );

        UserModel user = new UserModel();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(this.passwordEncoder.encode(request.getPassword()));
        user.setIsEnabled(request.getIsEnabled());
        user.setIsAccountNonExpired(request.getIsAccountNonExpired());
        user.setIsAccountNonLocked(request.getIsAccountNonLocked());
        user.setIsCredentialsNonExpired(request.getIsCredentialsNonExpired());
        user.setRoles(request.getRoles());

        return this.userRepository.save(user);
    }

    @Transactional
    public UserModel updateUserByUserId(UUID userId, UpdateUserByIdRequest request) {
        UserModel user = this.userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getIsEnabled() != null) {
            user.setIsEnabled(request.getIsEnabled());
        }

        if (request.getIsAccountNonExpired() != null) {
            user.setIsAccountNonExpired(request.getIsAccountNonExpired());
        }

        if (request.getIsAccountNonLocked() != null) {
            user.setIsAccountNonLocked(request.getIsAccountNonLocked());
        }

        if (request.getIsCredentialsNonExpired() != null) {
            user.setIsCredentialsNonExpired(request.getIsCredentialsNonExpired());
        }

        if (request.getRoles() != null) {
            user.setRoles(request.getRoles());
        }

        return this.userRepository.save(user);
    }

    @Transactional
    public UserModel deleteUserByUserId(UUID userId) {
        UserModel user = this.userRepository.findById(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("User not found with id: " + userId)
                );

        this.userRepository.delete(user);
        return user;
    }
}
