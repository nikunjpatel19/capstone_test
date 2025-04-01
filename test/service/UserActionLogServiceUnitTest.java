package tech.zodiac.px_um.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tech.zodiac.px_um.model.ActionType;
import tech.zodiac.px_um.model.Status;
import tech.zodiac.px_um.model.User;
import tech.zodiac.px_um.model.UserActionLog;
import tech.zodiac.px_um.repository.UserActionLogRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class UserActionLogServiceUnitTest {
    @Mock
    private UserActionLogRepository userActionLogRepository;

    @InjectMocks
    private UserActionLogService userActionLogService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddUserActionLog() {
        User user = new User();
        user.setId(1);
        ActionType actionType = ActionType.LOGIN;
        Status status = Status.SUCCESS;
        String details = "details";
        String ipAddress = "127.0.0.1";

        userActionLogService.addUserActionLog(user, actionType, status, details, ipAddress);

        verify(userActionLogRepository, times(1)).save(any(UserActionLog.class));
    }
}
