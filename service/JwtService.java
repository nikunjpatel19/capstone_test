package tech.zodiac.px_um.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tech.zodiac.px_um.exception.InvalidTokenException;
import tech.zodiac.px_um.exception.UserNotFoundException;
import tech.zodiac.px_um.model.RefreshToken;
import tech.zodiac.px_um.model.TokenType;
import tech.zodiac.px_um.model.User;
import tech.zodiac.px_um.repository.RefreshTokenRepository;
import tech.zodiac.px_um.repository.UserRepository;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private final long ACCESS_TOKEN_EXPIRATION_TIME = 1000 * 60 * 15; // 15 minutes
    private final long REFRESH_TOKEN_EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 7; // 7 days

    private final String PUBLIC_KEY_PATH;
    private final String PRIVATE_KEY_PATH;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final HashingService hashingService;

    private PublicKey publicKey;
    private PrivateKey privateKey;

    public JwtService(@Value("${jwt.public-key-path}") String publicKeyPath,
                      @Value("${jwt.private-key-path}") String privateKeyPath,
                      RefreshTokenRepository refreshTokenRepository,
                      UserRepository userRepository,
                      HashingService hashingService) {
        PUBLIC_KEY_PATH = publicKeyPath;
        PRIVATE_KEY_PATH = privateKeyPath;
        privateKey = loadPrivateKey();
        publicKey = loadPublicKey();
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.hashingService = hashingService;
    }

    public String generateAccessToken(Integer userId) {
        return generateTokenWithExpirationTime(userId, ACCESS_TOKEN_EXPIRATION_TIME, TokenType.ACCESS);
    }

    public String generateRefreshToken(Integer userId) {
        String refreshToken = generateTokenWithExpirationTime(userId, REFRESH_TOKEN_EXPIRATION_TIME, TokenType.REFRESH);
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            Instant expirationInstant = Instant.now().plusMillis(REFRESH_TOKEN_EXPIRATION_TIME);
            RefreshToken token = new RefreshToken(
                    userOptional.get(),
                    hashingService.hash(refreshToken),
                    LocalDateTime.ofInstant(expirationInstant, ZoneId.systemDefault())
            );
            refreshTokenRepository.save(token);
            return refreshToken;
        } else {
            throw new UserNotFoundException("User with UserId=" + userId + " does not exist");
        }
    }


    public boolean validateAccessToken(String token, Integer userId) {
        return validateToken(token, userId) && extractTokenType(token) == TokenType.ACCESS;
    }

    public boolean validateRefreshToken(String token, Integer userId) {
        return validateToken(token, userId)
                && refreshTokenRepository.findByRefreshToken(hashingService.hash(token)).isPresent()
                && extractTokenType(token) == TokenType.REFRESH;
    }

    public Integer extractUserId(String token) {
        return Integer.valueOf(getClaimsFromToken(token).getSubject());
    }

    public void deleteRefreshToken(String token) {
        refreshTokenRepository
                .findByRefreshToken(hashingService.hash(token))
                .ifPresentOrElse(refreshTokenRepository::delete,
                        () -> {throw new InvalidTokenException("Refresh token not found");}
                );
    }

    public void deleteRefreshTokenByUserId(Integer userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            refreshTokenRepository.deleteAllInBatch(refreshTokenRepository.findAllByUser(user));
        } else {
            throw new UserNotFoundException("User with UserId=" + userId + " does not exist");
        }
    }

    private boolean validateToken(String token, Integer userId) {
        return extractUserId(token).equals(userId);
    }

    private TokenType extractTokenType(String token) {
        return TokenType.valueOf(getClaimsFromToken(token).get("type", String.class));
    }

    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("Expired token");
        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid JWT token");
        }
    }

    private String generateTokenWithExpirationTime(Integer userId, long expirationTime, TokenType tokenType) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .claim("type", tokenType.name())
                .signWith(privateKey)
                .compact();
    }

    private PrivateKey loadPrivateKey() {

        try {
            String key = Files.readString(Paths.get(PRIVATE_KEY_PATH))
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(key);

            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(spec);

        } catch (Exception e) {
            throw new RuntimeException("Could not load private key", e);
        }
    }

    private PublicKey loadPublicKey() {
        try {
            String key = Files.readString(Paths.get(PUBLIC_KEY_PATH))
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(key);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            throw new RuntimeException("Could not load public key", e);
        }
    }
}
