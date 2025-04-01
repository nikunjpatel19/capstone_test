package tech.zodiac.px_um.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tech.zodiac.px_um.model.PasswordEventType;
import tech.zodiac.px_um.model.PasswordResetLog;
import tech.zodiac.px_um.model.User;
import tech.zodiac.px_um.repository.PasswordResetLogRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PasswordResetLogServiceUnitTest {
    @Mock
    private PasswordResetLogRepository passwordResetLogRepository;

    @InjectMocks
    private PasswordResetLogService passwordResetLogService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPasswordResetLogService() {
        User user = new User();
        user.setId(1);
        PasswordEventType passwordEventType = PasswordEventType.PASSWORD_CHANGED;

        passwordResetLogService.addPasswordResetLog(user, passwordEventType);

        verify(passwordResetLogRepository, times(1)).save(any(PasswordResetLog.class));
    }
}
