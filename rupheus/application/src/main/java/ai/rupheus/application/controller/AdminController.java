package ai.rupheus.application.controller;

import ai.rupheus.application.dto.shared.GenericResponse;
import ai.rupheus.application.dto.admin.CreateTargetRequest;
import ai.rupheus.application.dto.admin.CreateUserRequest;
import ai.rupheus.application.dto.admin.UpdateTargetRequest;
import ai.rupheus.application.dto.admin.UpdateUserRequest;
import ai.rupheus.application.model.target.TargetModel;
import ai.rupheus.application.model.user.UserModel;
import ai.rupheus.application.model.target.ConnectionScheme;
import ai.rupheus.application.service.AdminService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping("/user")
    public ResponseEntity<GenericResponse<?>> getUser(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String email,
            @ParameterObject Pageable pageable
    ) {
        if (userId != null) {
            UserModel fetchedUser = this.adminService.getUserByUserId(userId);
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

    @PostMapping("/user")
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

    @PatchMapping("/user")
    public ResponseEntity<GenericResponse<?>> updateUser(
            @RequestParam UUID userId,
            @RequestBody UpdateUserRequest updateUserRequest
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

    @DeleteMapping("/user")
    public ResponseEntity<GenericResponse<?>> deleteUser(
            @RequestParam List<UUID> userIds
    ) {
        List<UserModel> deletedUsers = this.adminService.deleteUserByUserIds(userIds);
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "Users deleted successfully",
                                deletedUsers

                        )
                );
    }

    @GetMapping("/target")
    public ResponseEntity<GenericResponse<?>> getTarget(
            @RequestParam(required = false) UUID targetId,
            @RequestParam(required = false) UUID userId,
            @ParameterObject Pageable pageable
    ) {
        if (targetId != null) {
            TargetModel fetchedTarget = this.adminService.getTargetByTargetId(targetId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(
                            new GenericResponse<>(
                                    HttpStatus.OK.value(),
                                    "Target fetched successfully",
                                    fetchedTarget
                            )
                    );
        }

        if (userId != null) {
            List<TargetModel> fetchedTarget = this.adminService.getTargetByUserId(userId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(
                            new GenericResponse<>(
                                    HttpStatus.OK.value(),
                                    "Target fetched successfully",
                                    fetchedTarget
                            )
                    );
        }

        Page<TargetModel> fetchedTargets = this.adminService.getAllTargets(pageable);
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "Targets fetched successfully",
                                fetchedTargets
                        )
                );
    }

    @PostMapping("/target")
    public ResponseEntity<GenericResponse<?>> createTarget(
            @RequestParam ConnectionScheme connectionScheme,
            @Valid @RequestBody CreateTargetRequest createTargetRequest
    ) {
        TargetModel createdTarget = this.adminService.createTarget(connectionScheme, createTargetRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        new GenericResponse<>(
                                HttpStatus.CREATED.value(),
                                "Targets created successfully",
                                createdTarget
                        )
                );
    }

    @PatchMapping("/target")
    public ResponseEntity<GenericResponse<?>> updateTarget(
            @RequestParam UUID targetId,
            @RequestBody UpdateTargetRequest updateTargetRequest
    ) {
        TargetModel updatedTarget = this.adminService.updateTargetByTargetId(targetId, updateTargetRequest);
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "Targets updated successfully",
                                updatedTarget
                        )
                );
    }

    @DeleteMapping("/target")
    public ResponseEntity<GenericResponse<?>> deleteTarget(
            @RequestParam List<UUID> targetIds
    ) {
        List<TargetModel> deletedTargets = this.adminService.deleteTargetByTargetIds(targetIds);
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "Targets deleted successfully",
                                deletedTargets
                        )
                );
    }
}
