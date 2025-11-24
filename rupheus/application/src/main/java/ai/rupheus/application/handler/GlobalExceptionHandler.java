package ai.rupheus.application.handler;

import ai.rupheus.application.dto.GenericResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        new GenericResponse<>(
                                HttpStatus.BAD_REQUEST.value(),
                                "An validation error occurred",
                                errors
                        )
                );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<GenericResponse<?>> handleEntityNotFoundException(
            EntityNotFoundException e
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(
                        new GenericResponse<>(
                                HttpStatus.NOT_FOUND.value(),
                                e.getMessage(),
                                null

                        )
                );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<GenericResponse<?>> handleIllegalStateException(
            IllegalStateException e
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(
                        new GenericResponse<>(
                                HttpStatus.CONFLICT.value(),
                                e.getMessage(),
                                null

                        )
                );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GenericResponse<?>> handleIllegalArgumentException(
            IllegalArgumentException e
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        new GenericResponse<>(
                                HttpStatus.BAD_REQUEST.value(),
                                e.getMessage(),
                                null
                        )
                );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GenericResponse<?>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        new GenericResponse<>(
                                HttpStatus.BAD_REQUEST.value(),
                                e.getMessage(),
                                null
                        )
                );
    }
}
