package tech.zodiac.px_um.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.zodiac.px_um.model.ActionType;
import tech.zodiac.px_um.model.Status;
import tech.zodiac.px_um.model.User;
import tech.zodiac.px_um.model.UserActionLog;
import tech.zodiac.px_um.repository.UserActionLogRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionLogService {
    private final UserActionLogRepository userActionLogRepository;

    @Transactional
    public void addUserActionLog(User user, ActionType actionType, Status status, String details, String ipAddress) {
        UserActionLog userActionLog = new UserActionLog();
        userActionLog.setUser(user);
        userActionLog.setActionType(actionType);
        userActionLog.setStatus(status);
        userActionLog.setDetails(details);
        userActionLog.setTimestamp(LocalDateTime.now());
        userActionLog.setIpAddress(ipAddress);

        userActionLogRepository.save(userActionLog);
        log.info("USER ACTION -- Action Type: {} Status: {} User: {} IP Address: {} At: {}", userActionLog.getActionType(), userActionLog.getStatus(), userActionLog.getUser().getId(), userActionLog.getIpAddress(), userActionLog.getTimestamp());
    }
}
