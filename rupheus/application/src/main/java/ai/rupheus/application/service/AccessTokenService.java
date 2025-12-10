package ai.rupheus.application.service;

import ai.rupheus.application.model.user.UserModel;
import ai.rupheus.application.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccessTokenService {
    @Value("${security.access_token.secret_key}")
    private String accessTokenSecretKey;

    @Value("${security.access_token.expiration}")
    private long accessTokenExpiration;

    private final UserRepository userRepository;

    @Autowired
    public AccessTokenService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    public Optional<UserModel> extractUserFromAccessToken(String accessToken) {
        return this.userRepository.findById(UUID.fromString(this.extract(accessToken, this.getSignInKey(this.accessTokenSecretKey)).getSubject()));
    }

    public String generateAccessToken(UUID userId) {
        return this.generate(userId.toString(), this.accessTokenExpiration, this.getSignInKey(this.accessTokenSecretKey));
    }

    private SecretKey getSignInKey(String secretKey) {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    private String generate(String subject, Long expireTime, SecretKey secretKey) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(secretKey)
                .compact();
    }

    private Claims extract(String token, SecretKey secretKey) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
