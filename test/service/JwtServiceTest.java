package tech.zodiac.px_um.service;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tech.zodiac.px_um.model.RefreshToken;
import tech.zodiac.px_um.model.TokenType;
import tech.zodiac.px_um.model.User;
import tech.zodiac.px_um.repository.RefreshTokenRepository;
import tech.zodiac.px_um.repository.UserRepository;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HashingService hashingService;

    @Mock
    private KeyLoader keyLoader;

    @InjectMocks
    private JwtService jwtService;
    private PrivateKey privateKey;
    private User testUser;

    @BeforeEach
    void setUp() throws Exception {

        this.privateKey = mock(PrivateKey.class);
        testUser = new User();
        testUser.setId(1);

        // Mock the KeyLoader behavior
        KeyLoader keyLoader = mock(KeyLoader.class);
        when(keyLoader.loadPrivateKey()).thenReturn(privateKey);

        jwtService = new JwtService(keyLoader, refreshTokenRepository, userRepository, hashingService);
    }

    @Test
    void testGenerateAccessToken() {
        String mockToken = "mocked.jwt.token";

        // Mock the static Jwts.builder() method using mockStatic
        try (MockedStatic<Jwts> mockedJwts = mockStatic(Jwts.class)) {
            // Create a mock JwtBuilder
            JwtBuilder jwtBuilder = mock(JwtBuilder.class);

            // Mock the static Jwts.builder() to return our mock JwtBuilder
            mockedJwts.when(Jwts::builder).thenReturn(jwtBuilder);

            // Mock the method chain on the JwtBuilder
            when(jwtBuilder.subject(anyString())).thenReturn(jwtBuilder);
            when(jwtBuilder.issuedAt(any(Date.class))).thenReturn(jwtBuilder);
            when(jwtBuilder.expiration(any(Date.class))).thenReturn(jwtBuilder);
            when(jwtBuilder.claim(eq("type"), eq(TokenType.ACCESS.name()))).thenReturn(jwtBuilder);

            // Mock the signWith call
            when(jwtBuilder.signWith(eq(privateKey))).thenReturn(jwtBuilder);
            when(jwtBuilder.compact()).thenReturn(mockToken);

            // Act: Generate access token
            String token = jwtService.generateAccessToken(testUser.getId());

            // Assert: Check that the token was generated and matches the mock value
            assertNotNull(token);
            assertEquals(mockToken, token);
        }
    }



    @Test
    void testGenerateRefreshToken() {
        String mockToken = "mocked.jwt.token";

        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));


        // Mock the static Jwts.builder() method using mockStatic
        try (MockedStatic<Jwts> mockedJwts = mockStatic(Jwts.class)) {
            // Create a mock JwtBuilder
            JwtBuilder jwtBuilder = mock(JwtBuilder.class);

            // Mock the static Jwts.builder() to return our mock JwtBuilder
            mockedJwts.when(Jwts::builder).thenReturn(jwtBuilder);

            // Mock the method chain on the JwtBuilder
            when(jwtBuilder.subject(anyString())).thenReturn(jwtBuilder);
            when(jwtBuilder.issuedAt(any(Date.class))).thenReturn(jwtBuilder);
            when(jwtBuilder.expiration(any(Date.class))).thenReturn(jwtBuilder);
            when(jwtBuilder.claim(eq("type"), eq(TokenType.REFRESH.name()))).thenReturn(jwtBuilder);

            // Mock the signWith call
            when(jwtBuilder.signWith(eq(privateKey))).thenReturn(jwtBuilder);
            when(jwtBuilder.compact()).thenReturn(mockToken);

            // Act: Generate refresh token
            String token = jwtService.generateRefreshToken(testUser.getId());

            // Assert: Check that the token was generated and matches the mock value
            assertNotNull(token);
            assertEquals(mockToken, token);
        }
    }

    @Test
    void testValidateAccessToken() {
        KeyPair keyPair = generateKeyPair();
        ReflectionTestUtils.setField(jwtService, "privateKey", keyPair.getPrivate());
        ReflectionTestUtils.setField(jwtService, "publicKey", keyPair.getPublic());

        String token = jwtService.generateAccessToken(testUser.getId());

        boolean isValid = jwtService.validateAccessToken(token, testUser.getId());

        assertTrue(isValid, "Access token should be valid");
    }

    @Test
    void testValidateRefreshToken() {
        String hashedToken = "hashed-refresh-token";
        RefreshToken refreshToken = new RefreshToken();
        KeyPair keyPair = generateKeyPair();
        ReflectionTestUtils.setField(jwtService, "privateKey", keyPair.getPrivate());
        ReflectionTestUtils.setField(jwtService, "publicKey", keyPair.getPublic());
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(hashingService.hash(anyString())).thenReturn(hashedToken);
        when(refreshTokenRepository.findByRefreshToken(anyString())).thenReturn(Optional.of(refreshToken));

        String token = jwtService.generateRefreshToken(testUser.getId());
        boolean isValid = jwtService.validateRefreshToken(token, testUser.getId());

        assertTrue(isValid, "Refresh token should be valid");
    }

    @Test
    void testExtractUserId() {
        KeyPair keyPair = generateKeyPair();
        ReflectionTestUtils.setField(jwtService, "privateKey", keyPair.getPrivate());
        ReflectionTestUtils.setField(jwtService, "publicKey", keyPair.getPublic());

        String token = jwtService.generateAccessToken(testUser.getId());
        Integer extractedUserId = jwtService.extractUserId(token);

        assertEquals(testUser.getId(), extractedUserId);
    }

    @Test
    void testDeleteRefreshToken() {
        String token = "valid-refresh-token";
        String hashedToken = "hashed-refresh-token";
        RefreshToken refreshToken = new RefreshToken();

        when(hashingService.hash(token)).thenReturn(hashedToken);
        when(refreshTokenRepository.findByRefreshToken(hashedToken)).thenReturn(Optional.of(refreshToken));

        jwtService.deleteRefreshToken(token);

        verify(refreshTokenRepository).delete(refreshToken);
    }

    @Test
    void testDeleteRefreshTokenByUserId() {
        List<RefreshToken> refreshTokens = List.of(new RefreshToken(), new RefreshToken());

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(refreshTokenRepository.findAllByUser(testUser)).thenReturn(refreshTokens);

        jwtService.deleteRefreshTokenByUserId(testUser.getId());

        verify(refreshTokenRepository).deleteAllInBatch(refreshTokens);
    }

    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate key pair", e);
        }
    }

}
