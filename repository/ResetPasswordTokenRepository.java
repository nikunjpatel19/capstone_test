package tech.zodiac.px_um.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.zodiac.px_um.model.ResetPasswordToken;
import tech.zodiac.px_um.model.User;

import java.util.Optional;

public interface ResetPasswordTokenRepository extends JpaRepository<ResetPasswordToken, Integer> {
    Optional<ResetPasswordToken> findByResetPasswordToken(String token);
    Optional<ResetPasswordToken> findByUser(User user);
}
