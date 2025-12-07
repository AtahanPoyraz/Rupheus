package ai.rupheus.application.config.security;

import ai.rupheus.application.config.logger.ApplicationLogger;
import ai.rupheus.application.model.UserModel;
import ai.rupheus.application.service.AccessTokenService;
import org.springframework.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {
    private final AccessTokenService accessTokenService;
    private final ApplicationLogger applicationLogger;

    @Autowired
    public AuthenticationFilter(
            AccessTokenService accessTokenService,
            ApplicationLogger applicationLogger
    ) {
        this.accessTokenService = accessTokenService;
        this.applicationLogger = applicationLogger;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest httpServletRequest,
            @NonNull HttpServletResponse httpServletResponse,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String accessToken = this.getAccessTokenFromCookies(httpServletRequest);
            if (accessToken == null || accessToken.isEmpty()) {
                filterChain.doFilter(httpServletRequest, httpServletResponse);
                return;
            }

            Optional<UserModel> user = this.accessTokenService.extractUserFromAccessToken(accessToken);
            if (user.isEmpty()) {
                filterChain.doFilter(httpServletRequest, httpServletResponse);
                return;
            }

            if (!user.get().isEnabled()) {
                filterChain.doFilter(httpServletRequest, httpServletResponse);
                return;
            }

            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                    new UsernamePasswordAuthenticationToken(user.get().getId(), null, user.get().getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } catch (Exception e) {
            this.applicationLogger.warn(AuthenticationFilter.class, "Access token parsing FAILED in AuthenticationFilter: " + e.getMessage());
            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private String getAccessTokenFromCookies(HttpServletRequest httpServletRequest) {
        if (httpServletRequest.getCookies() == null) {
            return null;
        }

        return Arrays.stream(httpServletRequest.getCookies())
                .filter(cookie -> "ACCESS_TOKEN".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}
