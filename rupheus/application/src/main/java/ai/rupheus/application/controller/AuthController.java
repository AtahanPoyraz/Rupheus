package ai.rupheus.application.controller;

import ai.rupheus.application.dto.GenericResponse;
import ai.rupheus.application.dto.auth.SignInRequest;
import ai.rupheus.application.dto.auth.SignUpRequest;
import ai.rupheus.application.model.UserModel;
import ai.rupheus.application.service.AuthService;
import ai.rupheus.application.service.JwtService;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final JwtService jwtService;
    private final AuthService authService;

    @Autowired
    public AuthController(
            JwtService jwtService,
            AuthService authService
    ) {
        this.jwtService = jwtService;
        this.authService = authService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<GenericResponse<?>> signUp(
            @Valid @RequestBody SignUpRequest request,
            @NonNull HttpServletResponse response
    ) {
        UserModel user = this.authService.signUp(request);
        String jwtToken = this.jwtService.generateJwtToken(user.getId());

        this.setJwtToken(jwtToken, response);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        new GenericResponse<>(
                                HttpStatus.CREATED.value(),
                                "User signed up successfully",
                                jwtToken
                        )
                );
    }

    @PostMapping("/sign-in")
    public ResponseEntity<GenericResponse<?>> signIn(
            @Valid @RequestBody SignInRequest request,
            @NonNull HttpServletResponse  response
    ) {
        UserModel user =  this.authService.signIn(request);
        String jwtToken = this.jwtService.generateJwtToken(user.getId());

        this.setJwtToken(jwtToken, response);
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "User signed in successfully",
                                jwtToken
                        )
                );
    }

    @GetMapping("/sign-out")
    public ResponseEntity<GenericResponse<?>> signOut(
            @NonNull HttpServletResponse  response
    ) {
        this.setJwtToken("", response);
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "User signed out successfully",
                                null
                        )
                );
    }

    private void setJwtToken(String token, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("SESSION_ID", token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(1))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
