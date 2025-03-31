package tech.zodiac.px_um.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import tech.zodiac.px_um.repository.VerificationTokenRepository;

import java.time.LocalDateTime;

@Service
@Component
public class VerificationTokenCleanUpService {
    private final Logger logger = LoggerFactory.getLogger(VerificationTokenCleanUpService.class);
    private final VerificationTokenRepository verificationTokenRepository;

    public VerificationTokenCleanUpService(VerificationTokenRepository verificationTokenRepository) {
        this.verificationTokenRepository = verificationTokenRepository;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void deleteAllExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        int deletedCount = verificationTokenRepository.findByExpiresTimeBefore(now).size();
        verificationTokenRepository.deleteByExpiresTimeBefore(now);
        logger.info("Deleted {} expired verification tokens", deletedCount);
    }
}
