package tech.zodiac.px_um.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Pageable;
import tech.zodiac.px_um.model.LogType;
import tech.zodiac.px_um.repository.EmailVerificationLogRepository;
import tech.zodiac.px_um.repository.PasswordResetLogRepository;
import tech.zodiac.px_um.repository.UserActionLogRepository;
import tech.zodiac.px_um.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class LogServiceUnitTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserActionLogRepository userActionLogRepository;

    @Mock
    private PasswordResetLogRepository passwordResetLogRepository;

    @Mock
    private EmailVerificationLogRepository emailVerificationLogRepository;

    @InjectMocks
    private LogService logService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUserActionLogs_UserAction() {
        when(userActionLogRepository.findAllByCriteria(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        List<?> logs = logService.getUserActionLogs(
                LogType.USER_ACTION, Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()
        );

        assertEquals(0, logs.size());
    }

    @Test
    void testGetUserActionLogs_PasswordReset() {
        when(passwordResetLogRepository.findAllByCriteria(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        List<?> logs = logService.getUserActionLogs(
                LogType.PASSWORD_RESET, Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()
        );

        assertEquals(0, logs.size());
    }

    @Test
    void testGetUserActionLogs_EmailVerification() {
        when(emailVerificationLogRepository.findAllByCriteria(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        List<?> logs = logService.getUserActionLogs(
                LogType.EMAIL_VERIFICATION, Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()
        );

        assertEquals(0, logs.size());
    }
}
