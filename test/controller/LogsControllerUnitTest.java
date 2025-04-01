package tech.zodiac.px_um.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tech.zodiac.px_um.model.ActionType;
import tech.zodiac.px_um.model.LogType;
import tech.zodiac.px_um.model.Status;
import tech.zodiac.px_um.service.LogService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class LogsControllerUnitTest {
    @Mock
    private LogService logService;

    @InjectMocks
    private LogsController logsController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUserLogs() {
        LogType logType = LogType.USER_ACTION;
        Optional<Integer> userId = Optional.of(1);
        Optional<String> event = Optional.of(ActionType.LOGIN.name());
        Optional<LocalDateTime> before = Optional.of(LocalDateTime.now());
        Optional<LocalDateTime> after = Optional.of(LocalDateTime.now().minusDays(1));
        Optional<String> status = Optional.of(Status.SUCCESS.name());
        Optional<String> ip = Optional.of("127.0.0.1");
        Optional<String> email = Optional.of("user@example.com");
        Optional<String> page = Optional.of("1");

        List<Object> mockLogs = Collections.emptyList();
        when(logService.getUserActionLogs(
                any(LogType.class),
                any(Optional.class), // userId
                any(Optional.class), // event
                any(Optional.class), // before
                any(Optional.class), // after
                any(Optional.class), // status
                any(Optional.class), // ip
                any(Optional.class), // email
                any(Optional.class)  // page
        )).thenReturn(mockLogs);

        ResponseEntity<List<?>> response = logsController.getUserLogs(
                logType, userId, event, before, after, status, ip, email, page
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockLogs, response.getBody());
    }
}
