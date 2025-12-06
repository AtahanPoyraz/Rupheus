package ai.rupheus.application.controller;

import ai.rupheus.application.dto.GenericResponse;
import ai.rupheus.application.dto.target.CreateTargetRequest;
import ai.rupheus.application.dto.target.UpdateTargetRequest;
import ai.rupheus.application.model.TargetModel;
import ai.rupheus.application.model.UserModel;
import ai.rupheus.application.model.enums.ConnectionScheme;
import ai.rupheus.application.service.TargetService;
import ai.rupheus.application.service.UserService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/target")
public class TargetController {
    private final TargetService targetService;
    private final UserService userService;

    @Autowired
    public TargetController(
            TargetService targetService,
            UserService userService
    ) {
        this.targetService = targetService;
        this.userService = userService;
    }

    @GetMapping("/get")
    public ResponseEntity<GenericResponse<?>> getTarget(
            @RequestParam(required = false) UUID targetId,
            @ParameterObject Pageable pageable
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

        if (targetId != null) {
            TargetModel fetchedTarget = this.targetService.getTargetById(fetchedUser.get().getId(), targetId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(
                            new GenericResponse<>(
                                    HttpStatus.OK.value(),
                                    "Target fetched successfully",
                                    fetchedTarget
                            )
                    );
        }

        List<TargetModel> fetchedTarget = this.targetService.getTargetByUserId(fetchedUser.get().getId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "Target fetched successfully",
                                fetchedTarget
                        )
                );
    }

    @PostMapping("/create")
    public ResponseEntity<GenericResponse<?>> createTarget(
            @RequestParam ConnectionScheme connectionScheme,
            @Valid @RequestBody CreateTargetRequest createTargetRequest
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

        TargetModel createdTarget = this.targetService.createTarget(fetchedUser.get(), connectionScheme, createTargetRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        new GenericResponse<>(
                                HttpStatus.CREATED.value(),
                                "Target created successfully",
                                createdTarget
                        )
                );
    }

    @PatchMapping("/update")
    public ResponseEntity<GenericResponse<?>> updateTarget(
            @RequestParam UUID targetId,
            @Valid @RequestBody UpdateTargetRequest updateTargetRequest
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

        TargetModel updatedTarget = this.targetService.updateTargetId(fetchedUser.get().getId(), targetId, updateTargetRequest);
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "Target updated successfully",
                                updatedTarget
                        )
                );
    }

    @DeleteMapping("/delete")
    public ResponseEntity<GenericResponse<?>> deleteTarget(
            @RequestParam UUID targetId
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

        TargetModel deletedTarget = this.targetService.deleteTargetById(fetchedUser.get().getId(), targetId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "Target deleted successfully",
                                deletedTarget
                        )
                );
    }

    @DeleteMapping("/bulk-delete")
    public ResponseEntity<GenericResponse<?>> bulkDeleteTarget(
            @RequestParam List<UUID> targetIds
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

        List<TargetModel> deletedTarget = this.targetService.bulkDeleteTargetByIds(fetchedUser.get().getId(), targetIds);
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "Target deleted successfully",
                                deletedTarget
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
