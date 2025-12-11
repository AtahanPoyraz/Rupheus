package ai.rupheus.application.handler;

import ai.rupheus.application.dto.shared.GenericResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse<?>> handleValidationExceptions(
            MethodArgumentNotValidException e
    ) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    errors.put(fieldName, errorMessage);
                }
        );

        return this.createGenericResponseForException(HttpStatus.BAD_REQUEST, "An validation error occurred", errors);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<GenericResponse<?>> handleEntityNotFoundException(
            EntityNotFoundException e
    ) {
        return this.createGenericResponseForException(HttpStatus.NOT_FOUND, e.getMessage(), null);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<GenericResponse<?>> handleIllegalStateException(
            IllegalStateException e
    ) {
        return this.createGenericResponseForException(HttpStatus.CONFLICT, e.getMessage(), null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GenericResponse<?>> handleIllegalArgumentException(
            IllegalArgumentException e
    ) {
        return this.createGenericResponseForException(HttpStatus.BAD_REQUEST, e.getMessage(), null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GenericResponse<?>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e
    ) {
        return this.createGenericResponseForException(HttpStatus.BAD_REQUEST, e.getMessage(), null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<GenericResponse<?>> handleDataIntegrityViolationException(
            DataIntegrityViolationException e
    ) {
        return this.createGenericResponseForException(HttpStatus.BAD_REQUEST, "Data integrity violation", null);
    }

    @ExceptionHandler({AuthenticationCredentialsNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<GenericResponse<?>> handleAuthenticationCredentialsNotFoundException(
            AuthenticationCredentialsNotFoundException e
    ) {
        return this.createGenericResponseForException(HttpStatus.UNAUTHORIZED, e.getMessage(), null);
    }

    private <T> ResponseEntity<GenericResponse<?>> createGenericResponseForException(
            HttpStatus status,
            String exceptionMessage,
            T data
    ) {
        return ResponseEntity.status(status)
                .body(
                        new GenericResponse<>(
                                status.value(),
                                exceptionMessage,
                                data
                        )
                );
    }
}
