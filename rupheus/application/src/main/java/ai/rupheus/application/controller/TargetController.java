package ai.rupheus.application.controller;

import ai.rupheus.application.dto.GenericResponse;
import ai.rupheus.application.dto.target.CreateTargetRequest;
import ai.rupheus.application.dto.target.Target;
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
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping
    public ResponseEntity<GenericResponse<?>> getTarget(
            @RequestParam(required = false) UUID targetId,
            @ParameterObject Pageable pageable
    ) {
        UserModel fetchedUser = this.getUserFromSecurityContext();
        if (targetId != null) {
            TargetModel fetchedTarget = this.targetService.getTargetByTargetId(fetchedUser.getId(), targetId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(
                            new GenericResponse<>(
                                    HttpStatus.OK.value(),
                                    "Target fetched successfully",
                                    Target.fromEntity(fetchedTarget)
                            )
                    );
        }

        List<TargetModel> fetchedTarget = this.targetService.getTargetByUserId(fetchedUser.getId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "Target fetched successfully",
                                Target.fromEntity(fetchedTarget)
                        )
                );
    }

    @PostMapping
    public ResponseEntity<GenericResponse<?>> createTarget(
            @RequestParam ConnectionScheme connectionScheme,
            @Valid @RequestBody CreateTargetRequest createTargetRequest
    ) {
        UserModel fetchedUser = this.getUserFromSecurityContext();
        TargetModel createdTarget = this.targetService.createTarget(fetchedUser, connectionScheme, createTargetRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        new GenericResponse<>(
                                HttpStatus.CREATED.value(),
                                "Target created successfully",
                                Target.fromEntity(createdTarget)
                        )
                );
    }

    @PatchMapping
    public ResponseEntity<GenericResponse<?>> updateTarget(
            @RequestParam UUID targetId,
            @RequestBody UpdateTargetRequest updateTargetRequest
    ) {
        UserModel fetchedUser = this.getUserFromSecurityContext();
        TargetModel updatedTarget = this.targetService.updateTargetByTargetId(fetchedUser.getId(), targetId, updateTargetRequest);
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "Target updated successfully",
                                Target.fromEntity(updatedTarget)
                        )
                );
    }

    @DeleteMapping
    public ResponseEntity<GenericResponse<?>> deleteTarget(
            @RequestParam List<UUID> targetIds
    ) {
        UserModel fetchedUser = this.getUserFromSecurityContext();
        List<TargetModel> deletedTarget = this.targetService.deleteTargetByTargetIds(fetchedUser.getId(), targetIds);
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "Target deleted successfully",
                                Target.fromEntity(deletedTarget)
                        )
                );
    }

    private UserModel getUserFromSecurityContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("Authentication required");
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof UUID userId)) {
            throw new BadCredentialsException("Invalid principal");
        }

        return userService.getUserByUserId(userId);
    }
}
