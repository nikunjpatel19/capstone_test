package tech.zodiac.px_um.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.zodiac.px_um.model.EmailEventType;
import tech.zodiac.px_um.model.EmailVerificationLog;
import tech.zodiac.px_um.model.User;
import tech.zodiac.px_um.repository.EmailVerificationLogRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationLogService {
    private final EmailVerificationLogRepository emailVerificationLogRepository;

    @Transactional
    public void addEmailVerificationLog(User user, EmailEventType emailEventType, String email) {
        EmailVerificationLog emailVerificationLog = new EmailVerificationLog();
        emailVerificationLog.setUser(user);
        emailVerificationLog.setEmail(email);
        emailVerificationLog.setEventType(emailEventType);
        emailVerificationLog.setTimestamp(LocalDateTime.now());

        emailVerificationLogRepository.save(emailVerificationLog);
        log.info("EMAIL EVENT -- Event Type: {} User: {} Email: {} At: {}", emailVerificationLog.getEventType(), emailVerificationLog.getUser().getId(), emailVerificationLog.getEmail(), emailVerificationLog.getTimestamp());
    }
}
