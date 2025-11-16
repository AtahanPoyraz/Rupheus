package ai.rupheus.application.service;

import ai.rupheus.application.model.UserModel;
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
public class JwtService {
    @Value("${security.jwt.secret_key}")
    private String jwtSecretKey;

    @Value("${security.jwt.reset_expiration}")
    private long jwtResetExpiration;

    private final UserRepository userRepository;

    @Autowired
    public JwtService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    public Optional<UserModel> extractUserFromJwtToken(String jwtToken) {
        SecretKey secretKey = this.getSignInKey(this.jwtSecretKey);
        Claims claims = this.extract(jwtToken, secretKey);
        UUID userId = UUID.fromString(claims.getSubject());
        return this.userRepository.findById(userId);
    }

    public String generateJwtToken(UUID userId) {
        SecretKey secretKey = this.getSignInKey(this.jwtSecretKey);
        return this.generate(userId.toString(), this.jwtResetExpiration, secretKey);
    }

    private SecretKey getSignInKey(String jwtSecretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
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
