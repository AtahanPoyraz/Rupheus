package ai.rupheus.application.controller;

import ai.rupheus.application.dto.GenericResponse;
import ai.rupheus.application.dto.user.CreateUserRequest;
import ai.rupheus.application.dto.user.UpdateUserByIdRequest;
import ai.rupheus.application.model.UserModel;
import ai.rupheus.application.service.JwtService;
import ai.rupheus.application.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.lang.NonNull;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;

    @Autowired
    public UserController(
            UserService userService,
            JwtService jwtService
    ) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @GetMapping("/me")
    public ResponseEntity<GenericResponse<?>> getCurrentUser(
            @NonNull HttpServletRequest request
    ) {
        String jwtToken = this.getTokenFromCookie(request);
        if (jwtToken == null || jwtToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(
                            new GenericResponse<>(
                                    HttpStatus.UNAUTHORIZED.value(),
                                    "Credentials are invalid",
                                    null
                            )
                    );
        }

        Optional<UserModel> user = this.jwtService.extractUserFromJwtToken(jwtToken);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(
                            new GenericResponse<>(
                                    HttpStatus.NOT_FOUND.value(),
                                    "User not found",
                                    null
                            )
                    );
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "User fetched successfully",
                                user.get()
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
            @Valid @RequestBody CreateUserRequest request
    ) {
        UserModel createdUser = this.userService.createUser(request);
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
            @Valid @RequestBody UpdateUserByIdRequest request
    ) {
        UserModel updatedUser = this.userService.updateUserByUserId(userId, request);
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

    private String getTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        return Arrays.stream(request.getCookies())
                .filter(cookie -> "SESSION_ID".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}
