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
    public UserModel signUp(SignUpRequest signUpRequest) {
        this.userRepository.findByEmail(signUpRequest.getEmail())
                .ifPresent(_ -> {throw new IllegalStateException("User already exists with email: " + signUpRequest.getEmail());}
        );

        UserModel signedUpUser = new UserModel();
        signedUpUser.setFirstName(signUpRequest.getFirstName());
        signedUpUser.setLastName(signUpRequest.getLastName());
        signedUpUser.setEmail(signUpRequest.getEmail());
        signedUpUser.setPassword(this.passwordEncoder.encode(signUpRequest.getPassword()));
        signedUpUser.setIsEnabled(true);
        signedUpUser.setIsAccountNonExpired(true);
        signedUpUser.setIsAccountNonLocked(true);
        signedUpUser.setIsCredentialsNonExpired(true);
        signedUpUser.setRoles(EnumSet.of(UserRole.ROLE_USER));

        return this.userRepository.save(signedUpUser);
    }

    public UserModel signIn(SignInRequest signInRequest) {
        return this.userRepository.findByEmail(signInRequest.getEmail())
                .filter(signedInUser -> this.passwordEncoder.matches(signInRequest.getPassword().trim(), signedInUser.getPassword()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
    }
}
