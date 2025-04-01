package tech.zodiac.px_um.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import tech.zodiac.px_um.repository.RefreshTokenRepository;
import tech.zodiac.px_um.repository.VerificationTokenRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

class ResetTokenCleanUpServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenCleanUpService refreshTokenCleanUpService;

    private final LocalDateTime fixedTime = LocalDateTime.of(2025, 3, 28, 22, 21, 7, 249386700);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDeleteAllExpiredTokens() {
        try (MockedStatic<LocalDateTime> mockedDateTime = Mockito.mockStatic(LocalDateTime.class)) {
            mockedDateTime.when(LocalDateTime::now).thenReturn(fixedTime);

            when(refreshTokenRepository.findByExpiresTimeBefore(fixedTime))
                    .thenReturn(List.of("token1", "token2"));

            refreshTokenCleanUpService.deleteAllExpiredTokens();

            verify(refreshTokenRepository, times(1)).findByExpiresTimeBefore(fixedTime);
            verify(refreshTokenRepository, times(1)).deleteByExpiresTimeBefore(fixedTime);
        }
    }
}