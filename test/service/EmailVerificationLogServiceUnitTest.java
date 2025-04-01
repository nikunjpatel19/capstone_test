package tech.zodiac.px_um.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tech.zodiac.px_um.model.EmailEventType;
import tech.zodiac.px_um.model.EmailVerificationLog;
import tech.zodiac.px_um.model.User;
import tech.zodiac.px_um.repository.EmailVerificationLogRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class EmailVerificationLogServiceUnitTest {
    @Mock
    private EmailVerificationLogRepository emailVerificationLogRepository;

    @InjectMocks
    private EmailVerificationLogService emailVerificationLogService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddEmailVerificationLog() {
        User user = new User();
        user.setId(1);
        String email = "test@example.com";
        EmailEventType emailEventType = EmailEventType.VERIFIED;

        emailVerificationLogService.addEmailVerificationLog(user, emailEventType, email);

        verify(emailVerificationLogRepository, times(1)).save(any(EmailVerificationLog.class));
    }
}
