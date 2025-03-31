package tech.zodiac.px_um.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.zodiac.px_um.model.ActionType;
import tech.zodiac.px_um.model.PasswordEventType;
import tech.zodiac.px_um.model.PasswordResetLog;
import tech.zodiac.px_um.model.User;
import tech.zodiac.px_um.repository.PasswordResetLogRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetLogService {
    private final PasswordResetLogRepository passwordResetLogRepository;

    @Transactional
    public void addPasswordResetLog(User user, PasswordEventType passwordEventType) {
        PasswordResetLog passwordResetLog = new PasswordResetLog();
        passwordResetLog.setUser(user);
        passwordResetLog.setTimestamp(LocalDateTime.now());
        passwordResetLog.setEventType(passwordEventType);

        passwordResetLogRepository.save(passwordResetLog);
        log.info("PASSWORD RESET -- Event Type: {} User: {} At: {}", passwordResetLog.getEventType(), passwordResetLog.getUser().getId(), passwordResetLog.getTimestamp());
    }
}
