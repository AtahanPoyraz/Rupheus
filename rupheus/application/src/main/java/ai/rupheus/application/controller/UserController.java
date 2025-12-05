package ai.rupheus.application.controller;

import ai.rupheus.application.dto.GenericResponse;
import ai.rupheus.application.dto.user.UpdatePasswordByIdRequest;
import ai.rupheus.application.dto.user.UpdateUserDetailsByIdRequest;
import ai.rupheus.application.dto.user.User;
import ai.rupheus.application.model.UserModel;
import ai.rupheus.application.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(
            UserService userService
    ) {
        this.userService = userService;
    }

    @GetMapping("/get")
    public ResponseEntity<GenericResponse<?>> me() {
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

    @PatchMapping("/update-details")
    public ResponseEntity<GenericResponse<?>> updateDetails(
            @Valid @RequestBody UpdateUserDetailsByIdRequest updateUserRequest
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

        UserModel updatedUser = this.userService.updateUserDetailsById(fetchedUser.get().getId(), updateUserRequest);
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "User updated successfully",
                                User.fromEntity(updatedUser)
                        )
                );
    }

    @PatchMapping("/update-password")
    public ResponseEntity<GenericResponse<?>> updatePassword(
            @Valid @RequestBody UpdatePasswordByIdRequest updatePasswordRequest
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

        UserModel updatedUser = this.userService.updatePasswordById(fetchedUser.get().getId(), updatePasswordRequest);
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "Password updated successfully",
                                User.fromEntity(updatedUser)
                        )
                );
    }

    @DeleteMapping("/delete")
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
            return Optional.of(this.userService.getUserById(userId));
        }

        return Optional.empty();
    }
}
