package ai.rupheus.application.controller;

import ai.rupheus.application.dto.shared.GenericResponse;
import ai.rupheus.application.dto.auth.SignInRequest;
import ai.rupheus.application.dto.auth.SignUpRequest;
import ai.rupheus.application.model.user.UserModel;
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
            @NonNull HttpServletRequest httpServletRequest,
            @Valid @RequestBody SignUpRequest signUpRequest,
            @NonNull HttpServletResponse httpServletResponse
    ) {
        UserModel user = this.authService.signUp(signUpRequest);

        this.revokeRefreshToken(httpServletRequest);

        String newAccessToken = this.accessTokenService.generateAccessToken(user.getId());
        String newRefreshToken = this.refreshTokenService.generateRefreshToken(user.getId());

        this.setAccessCookie(newAccessToken, httpServletResponse);
        this.setRefreshCookie(newRefreshToken, httpServletResponse);

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
            @NonNull HttpServletRequest httpServletRequest,
            @Valid @RequestBody SignInRequest signInRequest,
            @NonNull HttpServletResponse httpServletResponse
    ) {
        UserModel user = this.authService.signIn(signInRequest);

        this.revokeRefreshToken(httpServletRequest);

        String newAccessToken = this.accessTokenService.generateAccessToken(user.getId());
        String newRefreshToken = this.refreshTokenService.generateRefreshToken(user.getId());

        this.setAccessCookie(newAccessToken, httpServletResponse);
        this.setRefreshCookie(newRefreshToken, httpServletResponse);

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
            @NonNull HttpServletRequest httpServletRequest,
            @NonNull HttpServletResponse httpServletResponse
    ) {
        String refreshTokenRaw = this.getCookieValue(httpServletRequest, "REFRESH_TOKEN");
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

        UserModel user = this.refreshTokenService.findValidToken(refreshTokenRaw).getUser();

        this.revokeRefreshToken(httpServletRequest);

        String newRefreshToken = this.refreshTokenService.generateRefreshToken(user.getId());
        String newAccessToken = this.accessTokenService.generateAccessToken(user.getId());

        this.setAccessCookie(newAccessToken, httpServletResponse);
        this.setRefreshCookie(newRefreshToken, httpServletResponse);

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
            @NonNull HttpServletRequest httpServletRequest,
            @NonNull HttpServletResponse httpServletResponse
    ) {
        this.revokeRefreshToken(httpServletRequest);
        this.clearCookie("ACCESS_TOKEN", httpServletResponse);
        this.clearCookie("REFRESH_TOKEN", httpServletResponse);

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "User signed out successfully",
                                null
                        )
                );
    }

    private void revokeRefreshToken(HttpServletRequest httpServletRequest) {
        String refreshTokenRaw = this.getCookieValue(httpServletRequest, "REFRESH_TOKEN");
        if (refreshTokenRaw != null && !refreshTokenRaw.isEmpty()) {
            this.refreshTokenService.revokeToken(refreshTokenRaw);
        }
    }

    private void setAccessCookie(String token, HttpServletResponse httpServletResponse) {
        ResponseCookie cookie = ResponseCookie.from("ACCESS_TOKEN", token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(this.accessTokenExpiration / 1000)
                .build();

        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void setRefreshCookie(String token, HttpServletResponse httpServletResponse) {
        ResponseCookie cookie = ResponseCookie.from("REFRESH_TOKEN", token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(this.refreshTokenExpiration / 1000)
                .build();

        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearCookie(String name, HttpServletResponse httpServletResponse) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String getCookieValue(HttpServletRequest httpServletRequest, String name) {
        if (httpServletRequest.getCookies() == null) {
            return null;
        }

        return java.util.Arrays.stream(httpServletRequest.getCookies())
                .filter(c -> c.getName().equals(name))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
