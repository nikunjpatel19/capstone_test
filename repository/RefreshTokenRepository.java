package tech.zodiac.px_um.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import tech.zodiac.px_um.model.RefreshToken;
import tech.zodiac.px_um.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findByRefreshToken(String token);

    List<RefreshToken> findAllByUser(User user);

    @Transactional
    void deleteByExpiresTimeBefore(LocalDateTime dateTime);

    Collection<Object> findByExpiresTimeBefore(LocalDateTime dateTime);

    @Transactional
    void deleteByUserId(Integer userId);
}
