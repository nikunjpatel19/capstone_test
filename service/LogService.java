package tech.zodiac.px_um.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tech.zodiac.px_um.model.*;
import tech.zodiac.px_um.repository.EmailVerificationLogRepository;
import tech.zodiac.px_um.repository.PasswordResetLogRepository;
import tech.zodiac.px_um.repository.UserActionLogRepository;
import tech.zodiac.px_um.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LogService {
    private final int LOGS_PER_PAGE = 5;

    private final UserRepository userRepository;
    private final UserActionLogRepository userActionLogRepository;
    private final PasswordResetLogRepository passwordResetLogRepository;
    private final EmailVerificationLogRepository emailVerificationLogRepository;

    public LogService(UserRepository userRepository, UserActionLogRepository userActionLogRepository, PasswordResetLogRepository passwordResetLogRepository, EmailVerificationLogRepository emailVerificationLogRepository) {
        this.userRepository = userRepository;
        this.userActionLogRepository = userActionLogRepository;
        this.passwordResetLogRepository = passwordResetLogRepository;
        this.emailVerificationLogRepository = emailVerificationLogRepository;
    }

    public List<?> getUserActionLogs(LogType type,
                                     Optional<Integer> userIdOptional,
                                     Optional<String> eventTypeOptional,
                                     Optional<LocalDateTime> beforeOptional,
                                     Optional<LocalDateTime> afterOptional,
                                     Optional<String> statusOptional,
                                     Optional<String> ipAddressOptional,
                                     Optional<String> emailOptional,
                                     Optional<String> page) {

        Pageable pageable = getPageable(page);
        User user = userIdOptional.flatMap(userRepository::findById).orElse(null);
        LocalDateTime before = beforeOptional.orElse(null);
        LocalDateTime after = afterOptional.orElse(null);
        String email = emailOptional.orElse(null);
        String ipAddress = ipAddressOptional.orElse(null);
        Status status = statusOptional.map(s -> Status.valueOf(s.toUpperCase())).orElse(null);

        return switch (type) {
            case PASSWORD_RESET -> getPasswordResetLogs(user, before, after, eventTypeOptional, pageable);
            case EMAIL_VERIFICATION -> getEmailVerificationLogs(user, before, after, eventTypeOptional, email, pageable);
            case USER_ACTION -> getUserActionLogs(user, before, after, eventTypeOptional, ipAddress, status, pageable);
        };

    }

    private List<PasswordResetLog> getPasswordResetLogs(User user, LocalDateTime before,LocalDateTime after,
                                                        Optional<String> eventTypeOptional, Pageable pageable) {
        PasswordEventType passwordEventType = eventTypeOptional.map(s -> PasswordEventType.valueOf(s.toUpperCase())).orElse(null);
        return passwordResetLogRepository.findAllByCriteria(user, before, after, passwordEventType, pageable);
    }

    private List<EmailVerificationLog> getEmailVerificationLogs(User user, LocalDateTime before,LocalDateTime after,
                                                                Optional<String> eventTypeOptional, String email, Pageable pageable) {
        EmailEventType emailEventType = eventTypeOptional.map(s -> EmailEventType.valueOf(s.toUpperCase())).orElse(null);
        return emailVerificationLogRepository.findAllByCriteria(user, before, after, email, emailEventType, pageable);
    }

    private List<UserActionLog> getUserActionLogs(User user, LocalDateTime before,LocalDateTime after,
                                                         Optional<String> eventTypeOptional, String ipAddress, Status status, Pageable pageable) {
        ActionType actionType = eventTypeOptional.map(s -> ActionType.valueOf(s.toUpperCase())).orElse(null);
        return userActionLogRepository.findAllByCriteria(user, before, after, ipAddress, actionType, status, pageable);
    }

    private Pageable getPageable(Optional<String> page) {
        if (page.isPresent()) {
            String pageString = page.get();
            if (pageString.equalsIgnoreCase("all")) {
                return Pageable.unpaged();
            } else {
                int pageNumber = Integer.parseInt(pageString) - 1;
                return PageRequest.of(pageNumber, LOGS_PER_PAGE);
            }
        } else {
            return PageRequest.of(0, LOGS_PER_PAGE);
        }
    }
}
