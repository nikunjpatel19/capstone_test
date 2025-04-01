package tech.zodiac.px_um.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import tech.zodiac.px_um.dto.VerificationDto;
import tech.zodiac.px_um.service.VerificationService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class VerificationControllerUnitTest {
    @Mock
    private VerificationService verificationService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private VerificationController verificationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetVerifyLink() {
        VerificationDto verificationDto = new VerificationDto();
        when(verificationService.generateVerificationToken(verificationDto.getEmail())).thenReturn("Verification link sent");

        ResponseEntity<String> response = verificationController.getVerifyLink(verificationDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Verification link sent", response.getBody());
    }

    @Test
    void testResendVerify() {
        when(authentication.getName()).thenReturn("123");
        when(verificationService.resendVerificationToken(123)).thenReturn("Verification link resent");

        ResponseEntity<String> response = verificationController.resendVerify(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Verification link resent", response.getBody());
    }

    @Test
    void testVerifyUser() {
        String verifyToken = "valid-token";

        ResponseEntity<String> response = verificationController.verifyLink("Bearer " + verifyToken);

        verify(verificationService, times(1)).verifyUser(verifyToken);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User successfully verified", response.getBody());
    }

}
