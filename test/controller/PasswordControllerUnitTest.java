package tech.zodiac.px_um.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import tech.zodiac.px_um.dto.ChangePasswordDto;
import tech.zodiac.px_um.dto.ForgotPasswordDto;
import tech.zodiac.px_um.dto.ResetPasswordDto;
import tech.zodiac.px_um.service.PasswordService;
import tech.zodiac.px_um.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class PasswordControllerUnitTest {
    @Mock
    private PasswordService passwordService;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PasswordController passwordController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testForgotPassword() {
        ForgotPasswordDto forgotPasswordDto = new ForgotPasswordDto();
        when(passwordService.forgotPassword(forgotPasswordDto.getEmail())).thenReturn("Password reset email sent");

        ResponseEntity<String> response = passwordController.forgotPassword(forgotPasswordDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password reset email sent", response.getBody());
    }

    @Test
    void testResetPassword() {
        ResetPasswordDto resetPasswordDto = new ResetPasswordDto();
        String resetToken = "valid-token";

        ResponseEntity<String> response = passwordController.resetPassword("Bearer " + resetToken, resetPasswordDto);

        verify(passwordService, times(1)).resetPassword(resetToken, resetPasswordDto.getNewPassword());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password successfully reset", response.getBody());
    }

    @Test
    void testChangePassword() {
        ChangePasswordDto changePasswordDto = new ChangePasswordDto("oldPass123", "newPass456");
        when(authentication.getName()).thenReturn("123");

        ResponseEntity<String> response = passwordController.changePassword(changePasswordDto, authentication);

        verify(userService, times(1)).changePassword(123, "oldPass123", "newPass456");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password update successfully ", response.getBody());
    }
}
