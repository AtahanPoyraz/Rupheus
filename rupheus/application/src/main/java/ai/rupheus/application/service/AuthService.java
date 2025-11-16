package ai.rupheus.application.service;

import ai.rupheus.application.dto.auth.SignInRequest;
import ai.rupheus.application.dto.auth.SignUpRequest;
import ai.rupheus.application.model.UserModel;
import ai.rupheus.application.model.enums.UserRole;
import ai.rupheus.application.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserModel signUp(SignUpRequest request) {
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
        user.setIsEnabled(true);
        user.setIsAccountNonExpired(true);
        user.setIsAccountNonLocked(true);
        user.setIsCredentialsNonExpired(true);
        user.setRoles(EnumSet.of(UserRole.ROLE_USER));

        return this.userRepository.save(user);
    }

    public UserModel signIn(SignInRequest request) {
        return this.userRepository.findByEmail(request.getEmail())
                .filter(user -> this.passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid credentials")
                );
    }
}
