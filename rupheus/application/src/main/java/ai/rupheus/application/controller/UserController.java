package ai.rupheus.application.controller;

import ai.rupheus.application.dto.GenericResponse;
import ai.rupheus.application.dto.user.UpdatePasswordByIdRequest;
import ai.rupheus.application.dto.user.UpdateUserDetailsByIdRequest;
import ai.rupheus.application.dto.user.User;
import ai.rupheus.application.model.UserModel;
import ai.rupheus.application.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Autowired
    public UserController(
            UserService userService,
            ObjectMapper objectMapper
    ) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<GenericResponse<?>> getUser() {
        Optional<UserModel> fetchedUser = this.getUserFromSecurityContext();
        if (fetchedUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(
                            new GenericResponse<>(
                                    HttpStatus.UNAUTHORIZED.value(),
                                    "Credentials are invalid",
                                    null
                            )
                    );
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "User fetched successfully",
                                User.fromEntity(fetchedUser.get())
                        )
                );
    }

    @PatchMapping
    public ResponseEntity<GenericResponse<?>> updateUser(
            @RequestParam @Pattern(regexp = "details|password", message = "Invalid sectionType") String sectionType,
            @RequestBody  Map<String, Object> updateRequest
    ) {
        Optional<UserModel> fetchedUser = this.getUserFromSecurityContext();
        if (fetchedUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(
                            new GenericResponse<>(
                                    HttpStatus.UNAUTHORIZED.value(),
                                    "Credentials are invalid",
                                    null
                            )
                    );
        }

        if (sectionType.equals("details")) {
            UpdateUserDetailsByIdRequest updateUserDetailsByIdRequest =
                    objectMapper.convertValue(updateRequest, UpdateUserDetailsByIdRequest.class);

            UserModel updatedUser = userService.updateUserDetailsByUserId(fetchedUser.get().getId(), updateUserDetailsByIdRequest);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(
                            new GenericResponse<>(
                                    HttpStatus.OK.value(),
                                    "User updated successfully",
                                    User.fromEntity(updatedUser)
                            )
                    );
        }

        if (sectionType.equals("password")) {
            UpdatePasswordByIdRequest updatePasswordByIdRequest =
                    objectMapper.convertValue(updateRequest, UpdatePasswordByIdRequest.class);

            UserModel updatedUser = userService.updatePasswordByUserId(fetchedUser.get().getId(), updatePasswordByIdRequest);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(
                            new GenericResponse<>(
                                    HttpStatus.OK.value(),
                                    "User updated successfully",
                                    User.fromEntity(updatedUser)
                            )
                    );
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        new GenericResponse<>(
                                HttpStatus.BAD_REQUEST.value(),
                                "Section type is invalid",
                                null
                        )
                );
    }

    @DeleteMapping
    public ResponseEntity<GenericResponse<?>> deleteUser() {
        Optional<UserModel> fetchedUser = this.getUserFromSecurityContext();
        if (fetchedUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(
                            new GenericResponse<>(
                                    HttpStatus.UNAUTHORIZED.value(),
                                    "Credentials are invalid",
                                    null
                            )
                    );
        }

        UserModel deletedUser = this.userService.deleteUserByUserId(fetchedUser.get().getId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "User deleted successfully",
                                deletedUser

                        )
                );
    }

    private Optional<UserModel> getUserFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UUID userId) {
            return Optional.of(this.userService.getUserByUserId(userId));
        }

        return Optional.empty();
    }
}
