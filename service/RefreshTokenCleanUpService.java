package tech.zodiac.px_um.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import tech.zodiac.px_um.repository.RefreshTokenRepository;

import java.time.LocalDateTime;

@Service
@Component
public class RefreshTokenCleanUpService {
    private final Logger logger = LoggerFactory.getLogger(RefreshTokenCleanUpService.class);
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenCleanUpService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void deleteAllExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        int deletedCount = refreshTokenRepository.findByExpiresTimeBefore(now).size();
        refreshTokenRepository.deleteByExpiresTimeBefore(now);
        logger.info("Deleted {} expired refresh tokens", deletedCount);
    }
}
