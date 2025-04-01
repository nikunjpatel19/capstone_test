package tech.zodiac.px_um.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import tech.zodiac.px_um.exception.InvalidTokenException;
import tech.zodiac.px_um.exception.UserNotFoundException;
import tech.zodiac.px_um.model.EmailEventType;
import tech.zodiac.px_um.model.User;
import tech.zodiac.px_um.model.VerificationToken;
import tech.zodiac.px_um.repository.UserRepository;
import tech.zodiac.px_um.repository.VerificationTokenRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VerificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HashingService hashingService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private EmailVerificationLogService emailVerificationLogService;

    @InjectMocks
    private VerificationService verificationService;

    private final String testEmail = "test@example.com";
    private final Integer testUserId = 1;
    private final String testToken = UUID.randomUUID().toString();
    private final String hashedToken = "hashed-" + testToken;
    private final String frontEndUrl = "http://localhost:3000";
    private final User testUser = new User();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        verificationService = new VerificationService(frontEndUrl);
        MockitoAnnotations.openMocks(this);

        testUser.setId(testUserId);
        testUser.setEmail(testEmail);
        testUser.setVerified(false);
    }

    @Test
    void generateVerificationToken_UserExists_CreatesNewToken() {
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(hashingService.hash(anyString())).thenReturn(hashedToken);
        when(verificationTokenRepository.findByUser(testUser)).thenReturn(Optional.empty());

        String resultUrl = verificationService.generateVerificationToken(testEmail);

        assertTrue(resultUrl.startsWith(frontEndUrl + "/verify-user?token="));
        verify(verificationTokenRepository).save(any(VerificationToken.class));
        verify(emailVerificationLogService).addEmailVerificationLog(
                testUser, EmailEventType.SENT, testEmail);
    }

    @Test
    void generateVerificationToken_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                verificationService.generateVerificationToken(testEmail));
    }

    @Test
    void generateVerificationToken_ExistingToken_DeletesOldToken() {
        VerificationToken existingToken = new VerificationToken();
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(verificationTokenRepository.findByUser(testUser)).thenReturn(Optional.of(existingToken));

        verificationService.generateVerificationToken(testEmail);

        verify(verificationTokenRepository).delete(existingToken);
    }

    @Test
    void resendVerificationToken_UserExists_GeneratesNewToken() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(hashingService.hash(anyString())).thenReturn(hashedToken);
        when(verificationTokenRepository.findByUser(testUser)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        String resultUrl = verificationService.resendVerificationToken(testUserId);

        assertTrue(resultUrl.startsWith(frontEndUrl + "/verify-user?token="));
    }

    @Test
    void resendVerificationToken_UserNotFound_ThrowsException() {

        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());


        assertThrows(UserNotFoundException.class, () ->
                verificationService.resendVerificationToken(testUserId));
    }

    @Test
    void verifyUser_ValidToken_VerifiesUser() {

        VerificationToken validToken = createValidToken();
        when(hashingService.hash(testToken)).thenReturn(hashedToken);
        when(verificationTokenRepository.findBytoken(hashedToken)).thenReturn(Optional.of(validToken));


        verificationService.verifyUser(testToken);


        assertTrue(testUser.isVerified());
        verify(userRepository).save(testUser);
        verify(verificationTokenRepository).delete(validToken);
        verify(emailVerificationLogService).addEmailVerificationLog(
                testUser, EmailEventType.VERIFIED, null);
    }

    @Test
    void verifyUser_InvalidToken_ThrowsException() {

        when(hashingService.hash(testToken)).thenReturn(hashedToken);
        when(verificationTokenRepository.findBytoken(hashedToken)).thenReturn(Optional.empty());


        assertThrows(InvalidTokenException.class, () ->
                verificationService.verifyUser(testToken));
    }

    @Test
    void verifyUser_ExpiredToken_ThrowsException() {
        // Arrange
        VerificationToken expiredToken = createExpiredToken();
        when(hashingService.hash(testToken)).thenReturn(hashedToken);
        when(verificationTokenRepository.findBytoken(hashedToken)).thenReturn(Optional.of(expiredToken));

        // Act & Assert
        assertThrows(InvalidTokenException.class, () ->
                verificationService.verifyUser(testToken));
        verify(verificationTokenRepository).delete(expiredToken);
    }

    private VerificationToken createValidToken() {
        VerificationToken token = new VerificationToken();
        token.setToken(hashedToken);
        token.setUser(testUser);
        token.setExpiresTime(LocalDateTime.now().plusHours(1));
        return token;
    }

    private VerificationToken createExpiredToken() {
        VerificationToken token = new VerificationToken();
        token.setToken(hashedToken);
        token.setUser(testUser);
        token.setExpiresTime(LocalDateTime.now().minusHours(1));
        return token;
    }
}