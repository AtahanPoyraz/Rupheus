package ai.rupheus.application.controller;

import ai.rupheus.application.dto.GenericResponse;
import ai.rupheus.application.dto.user.CreateUserRequest;
import ai.rupheus.application.dto.user.UpdateUserByIdRequest;
import ai.rupheus.application.dto.user.User;
import ai.rupheus.application.model.UserModel;
import ai.rupheus.application.service.UserService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

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

    @GetMapping("/me")
    public ResponseEntity<GenericResponse<?>> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(
                            new GenericResponse<>(
                                    HttpStatus.UNAUTHORIZED.value(),
                                    "Credentials are invalid",
                                    null
                            )
                    );
        }

        UserModel fetchedUser = (UserModel) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "User fetched successfully",
                                User.fromEntity(fetchedUser)
                        )
                );
    }

    @GetMapping("/get")
    public ResponseEntity<GenericResponse<?>> getUser(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String email,
            @ParameterObject Pageable pageable
    ) {
        if (userId != null) {
            UserModel fetchedUser = this.userService.getUserById(userId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(
                            new GenericResponse<>(
                                    HttpStatus.OK.value(),
                                    "User fetched successfully",
                                    fetchedUser
                            )
                    );
        }

        if (email != null) {
            UserModel fetchedUser = this.userService.getUserByEmail(email);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(
                            new GenericResponse<>(
                                    HttpStatus.OK.value(),
                                    "User fetched successfully",
                                    fetchedUser
                            )
                    );
        }

        Page<UserModel> fetchedUsers = this.userService.getAllUsers(pageable);
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "Users fetched successfully",
                                fetchedUsers
                        )
                );
    }

    @PostMapping("/create")
    public ResponseEntity<GenericResponse<?>> createUser(
            @Valid @RequestBody CreateUserRequest createUserRequest
    ) {
        UserModel createdUser = this.userService.createUser(createUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        new GenericResponse<>(
                                HttpStatus.CREATED.value(),
                                "User created successfully",
                                createdUser
                        )
                );
    }

    @PatchMapping("/update")
    public ResponseEntity<GenericResponse<?>> updateUser(
            @RequestParam UUID userId,
            @Valid @RequestBody UpdateUserByIdRequest updateUserRequest
    ) {
        UserModel updatedUser = this.userService.updateUserByUserId(userId, updateUserRequest);
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "User updated successfully",
                                updatedUser
                        )
                );
    }

    @DeleteMapping("/delete")
    public ResponseEntity<GenericResponse<?>> deleteUser(
            @RequestParam UUID userId
    ) {
        UserModel deletedUser = this.userService.deleteUserByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "User deleted successfully",
                                deletedUser

                        )
                );
    }
}
