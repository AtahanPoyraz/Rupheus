package ai.rupheus.application.service;

import ai.rupheus.application.dto.user.UpdatePasswordByIdRequest;
import ai.rupheus.application.dto.user.UpdateUserDetailsByIdRequest;
import ai.rupheus.application.model.user.UserModel;
import ai.rupheus.application.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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

    public UserModel getUserByUserId(UUID userId) {
        return this.userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId.toString()));
    }

    @Transactional
    public UserModel updateUserDetailsByUserId(UUID userId, UpdateUserDetailsByIdRequest updateUserDetailsRequest) {
        UserModel updatedUser = this.userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (updateUserDetailsRequest.getFirstName() != null && !updateUserDetailsRequest.getFirstName().isEmpty()) {
            updatedUser.setFirstName(updateUserDetailsRequest.getFirstName());
        }

        if (updateUserDetailsRequest.getLastName() != null && !updateUserDetailsRequest.getLastName().isEmpty()) {
            updatedUser.setLastName(updateUserDetailsRequest.getLastName());
        }

        return this.userRepository.save(updatedUser);
    }

    @Transactional
    public UserModel updatePasswordByUserId(UUID userId, UpdatePasswordByIdRequest updatePasswordRequest) {
        UserModel updatedUser = this.userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (!this.passwordEncoder.matches(updatePasswordRequest.getPassword(), updatedUser.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        updatedUser.setPassword(this.passwordEncoder.encode(updatePasswordRequest.getNewPassword()));

        return this.userRepository.save(updatedUser);
    }

    @Transactional
    public UserModel deleteUserByUserId(UUID userId) {
        UserModel deletedUser = this.userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        deletedUser.setIsEnabled(false);
        deletedUser.setIsAccountNonExpired(false);
        deletedUser.setIsAccountNonLocked(false);
        deletedUser.setIsCredentialsNonExpired(false);

        return this.userRepository.save(deletedUser);
    }
}
