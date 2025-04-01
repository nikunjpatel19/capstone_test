package tech.zodiac.px_um.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import tech.zodiac.px_um.exception.*;
import tech.zodiac.px_um.model.*;
import tech.zodiac.px_um.repository.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordServiceTest {

    @Mock
    private ResetPasswordTokenRepository resetPasswordTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private HashingService hashingService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordResetLogService passwordResetLogService;

    @Mock
    private EmailVerificationLogService emailVerificationLogService;

    @InjectMocks
    private PasswordService passwordService;

    private final String testEmail = "test@example.com";
    private final String testToken = UUID.randomUUID().toString();
    private final String hashedToken = "hashed-" + testToken;
    private final String newPassword = "ValidPass123!";
    private final String frontEndUrl = "http://localhost:3000";
    private final User testUser = new User();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordService = new PasswordService(frontEndUrl);
        MockitoAnnotations.openMocks(this);

        testUser.setEmail(testEmail);
        testUser.setPassword("oldPassword");
    }

    @Test
    void forgotPassword_UserExists_GeneratesNewToken() {
        // Arrange
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(hashingService.hash(anyString())).thenReturn(hashedToken);
        when(resetPasswordTokenRepository.findByUser(testUser)).thenReturn(Optional.empty());

        // Act
        String resultUrl = passwordService.forgotPassword(testEmail);

        // Assert
        assertTrue(resultUrl.startsWith(frontEndUrl + "/reset-password?token="));
        verify(resetPasswordTokenRepository).save(any(ResetPasswordToken.class));
        verify(passwordResetLogService).addPasswordResetLog(testUser, PasswordEventType.URL_CREATED);
        verify(emailVerificationLogService).addEmailVerificationLog(testUser, EmailEventType.SENT, testEmail);
    }

    @Test
    void forgotPassword_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> passwordService.forgotPassword(testEmail));
    }

    @Test
    void forgotPassword_ExistingToken_DeletesOldToken() {
        // Arrange
        ResetPasswordToken existingToken = new ResetPasswordToken();
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(resetPasswordTokenRepository.findByUser(testUser)).thenReturn(Optional.of(existingToken));

        // Act
        passwordService.forgotPassword(testEmail);

        // Assert
        verify(resetPasswordTokenRepository).delete(existingToken);
    }

    @Test
    void resetPassword_ValidToken_UpdatesPassword() {
        // Arrange
        ResetPasswordToken validToken = createValidToken();
        when(hashingService.hash(testToken)).thenReturn(hashedToken);
        when(resetPasswordTokenRepository.findByResetPasswordToken(hashedToken)).thenReturn(Optional.of(validToken));
        when(userService.isValidPassword(newPassword)).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        // Act
        passwordService.resetPassword(testToken, newPassword);

        // Assert
        assertEquals("encodedNewPassword", testUser.getPassword());
        verify(userRepository).save(testUser);
        verify(resetPasswordTokenRepository).delete(validToken);
        verify(passwordResetLogService).addPasswordResetLog(testUser, PasswordEventType.PASSWORD_CHANGED);
        verify(refreshTokenRepository).deleteAllInBatch(any());
    }

    @Test
    void resetPassword_InvalidToken_ThrowsException() {
        // Arrange
        when(hashingService.hash(testToken)).thenReturn(hashedToken);
        when(resetPasswordTokenRepository.findByResetPasswordToken(hashedToken)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> passwordService.resetPassword(testToken, newPassword));
    }

    @Test
    void resetPassword_ExpiredToken_ThrowsException() {
        // Arrange
        ResetPasswordToken expiredToken = createExpiredToken();
        when(hashingService.hash(testToken)).thenReturn(hashedToken);
        when(resetPasswordTokenRepository.findByResetPasswordToken(hashedToken)).thenReturn(Optional.of(expiredToken));

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> passwordService.resetPassword(testToken, newPassword));
        verify(resetPasswordTokenRepository).delete(expiredToken);
    }

    @Test
    void resetPassword_WeakPassword_ThrowsException() {
        // Arrange
        ResetPasswordToken validToken = createValidToken();
        when(hashingService.hash(testToken)).thenReturn(hashedToken);
        when(resetPasswordTokenRepository.findByResetPasswordToken(hashedToken)).thenReturn(Optional.of(validToken));
        when(userService.isValidPassword(newPassword)).thenReturn(false);

        // Act & Assert
        assertThrows(PasswordException.class, () -> passwordService.resetPassword(testToken, newPassword));
    }

    private ResetPasswordToken createValidToken() {
        ResetPasswordToken token = new ResetPasswordToken();
        token.setResetPasswordToken(hashedToken);
        token.setUser(testUser);
        token.setExpiresTime(LocalDateTime.now().plusHours(1));
        return token;
    }

    private ResetPasswordToken createExpiredToken() {
        ResetPasswordToken token = new ResetPasswordToken();
        token.setResetPasswordToken(hashedToken);
        token.setUser(testUser);
        token.setExpiresTime(LocalDateTime.now().minusHours(1));
        return token;
    }
}