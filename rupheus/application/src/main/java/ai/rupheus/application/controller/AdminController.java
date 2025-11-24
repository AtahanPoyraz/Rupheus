package ai.rupheus.application.controller;

import ai.rupheus.application.dto.GenericResponse;
import ai.rupheus.application.dto.admin.CreateUserRequest;
import ai.rupheus.application.dto.admin.UpdateUserByIdRequest;
import ai.rupheus.application.model.UserModel;
import ai.rupheus.application.service.AdminService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final AdminService adminService;

    @Autowired
    public AdminController(
            AdminService adminService
    ) {
        this.adminService = adminService;
    }

    @GetMapping("/get")
    public ResponseEntity<GenericResponse<?>> getUser(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String email,
            @ParameterObject Pageable pageable
    ) {
        if (userId != null) {
            UserModel fetchedUser = this.adminService.getUserById(userId);
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
            UserModel fetchedUser = this.adminService.getUserByEmail(email);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(
                            new GenericResponse<>(
                                    HttpStatus.OK.value(),
                                    "User fetched successfully",
                                    fetchedUser
                            )
                    );
        }

        Page<UserModel> fetchedUsers = this.adminService.getAllUsers(pageable);
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
        UserModel createdUser = this.adminService.createUser(createUserRequest);
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
        UserModel updatedUser = this.adminService.updateUserByUserId(userId, updateUserRequest);
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
        UserModel deletedUser = this.adminService.deleteUserByUserId(userId);
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
