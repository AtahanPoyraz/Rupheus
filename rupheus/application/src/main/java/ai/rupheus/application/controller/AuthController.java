package ai.rupheus.application.controller;

import ai.rupheus.application.dto.GenericResponse;
import ai.rupheus.application.dto.auth.SignInRequest;
import ai.rupheus.application.dto.auth.SignUpRequest;
import ai.rupheus.application.model.RefreshTokenModel;
import ai.rupheus.application.model.UserModel;
import ai.rupheus.application.service.AccessTokenService;
import ai.rupheus.application.service.AuthService;
import ai.rupheus.application.service.RefreshTokenService;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Value("${security.access_token.expiration}")
    private long accessTokenExpiration;

    @Value("${security.refresh_token.expiration}")
    private long refreshTokenExpiration;

    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final AuthService authService;

    @Autowired
    public AuthController(
            AccessTokenService accessTokenService,
            RefreshTokenService refreshTokenService,
            AuthService authService
    ) {
        this.accessTokenService = accessTokenService;
        this.refreshTokenService = refreshTokenService;
        this.authService = authService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<GenericResponse<?>> signUp(
            @Valid @RequestBody SignUpRequest request,
            @NonNull HttpServletResponse response
    ) {
        UserModel user = this.authService.signUp(request);

        String accessToken = this.accessTokenService.generateAccessToken(user.getId());
        String refreshToken = this.refreshTokenService.generateRefreshToken(user.getId());

        this.setAccessCookie(accessToken, response);
        this.setRefreshCookie(refreshToken, response);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        new GenericResponse<>(
                                HttpStatus.CREATED.value(),
                                "User signed up successfully",
                                null
                        )
                );
    }

    @PostMapping("/sign-in")
    public ResponseEntity<GenericResponse<?>> signIn(
            @Valid @RequestBody SignInRequest request,
            @NonNull HttpServletResponse response
    ) {
        UserModel user = this.authService.signIn(request);

        String accessToken = this.accessTokenService.generateAccessToken(user.getId());
        String refreshToken = this.refreshTokenService.generateRefreshToken(user.getId());

        this.setAccessCookie(accessToken, response);
        this.setRefreshCookie(refreshToken, response);

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "User signed in successfully",
                                null
                        )
                );
    }

    @PostMapping("/refresh")
    public ResponseEntity<GenericResponse<?>> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshTokenRaw = this.getCookieValue(request, "REFRESH_TOKEN");
        if (refreshTokenRaw == null || refreshTokenRaw.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(
                            new GenericResponse<>(
                                    HttpStatus.UNAUTHORIZED.value(),
                                    "Refresh token missing",
                                    null
                            )
                    );
        }

        Optional<RefreshTokenModel> storedToken =
                this.refreshTokenService.findValidToken(refreshTokenRaw);

        if (storedToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(
                            new GenericResponse<>(
                                    HttpStatus.UNAUTHORIZED.value(),
                                    "Refresh token invalid or expired",
                                    null
                            )
                    );
        }

        this.refreshTokenService.revokeToken(refreshTokenRaw);

        UserModel user = storedToken.get().getUser();

        String newRefreshToken = this.refreshTokenService.generateRefreshToken(user.getId());
        String newAccessToken = this.accessTokenService.generateAccessToken(user.getId());

        this.setAccessCookie(newAccessToken, response);
        this.setRefreshCookie(newRefreshToken, response);

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "Access token refreshed",
                                null
                        )
                );
    }

    @PostMapping("/sign-out")
    public ResponseEntity<GenericResponse<?>> signOut(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshTokenRaw = this.getCookieValue(request, "REFRESH_TOKEN");
        if (refreshTokenRaw != null) {
            this.refreshTokenService.revokeToken(refreshTokenRaw);
        }

        this.clearCookie("ACCESS_TOKEN", response);
        this.clearCookie("REFRESH_TOKEN", response);

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "User signed out successfully",
                                null
                        )
                );
    }

    private void setAccessCookie(String token, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("ACCESS_TOKEN", token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(this.accessTokenExpiration / 1000)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void setRefreshCookie(String token, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("REFRESH_TOKEN", token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(this.refreshTokenExpiration / 1000)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearCookie(String name, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }

        return java.util.Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals(name))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}