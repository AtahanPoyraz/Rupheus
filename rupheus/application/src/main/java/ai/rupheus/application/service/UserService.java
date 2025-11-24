package ai.rupheus.application.service;

import ai.rupheus.application.dto.user.UpdateUserByIdRequest;
import ai.rupheus.application.model.UserModel;
import ai.rupheus.application.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    public UserModel getUserById(UUID userId) {
        return this.userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId.toString()));
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

        return this.userRepository.save(user);
    }

    @Transactional
    public UserModel deleteUserByUserId(UUID userId) {
        UserModel user = this.userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        user.setIsEnabled(false);
        user.setIsAccountNonExpired(false);
        user.setIsAccountNonLocked(false);
        user.setIsCredentialsNonExpired(false);

        return this.userRepository.save(user);
    }
}
