package tech.zodiac.px_um.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import tech.zodiac.px_um.model.User;
import tech.zodiac.px_um.model.VerificationToken;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Integer> {
    Optional<VerificationToken> findBytoken(String token);
    Optional<VerificationToken> findByUser(User user);
    @Transactional
    void deleteByExpiresTimeBefore(LocalDateTime dateTime);

    Collection<Object> findByExpiresTimeBefore(LocalDateTime dateTime);
}
