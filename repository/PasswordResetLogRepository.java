package tech.zodiac.px_um.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.zodiac.px_um.model.PasswordEventType;
import tech.zodiac.px_um.model.PasswordResetLog;
import tech.zodiac.px_um.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface PasswordResetLogRepository extends JpaRepository<PasswordResetLog,Integer> {

    @Query("SELECT p FROM PasswordResetLog p "+
            "WHERE (:user IS NULL OR p.user = :user) " +
            "AND (:before IS NULL OR p.timestamp <= :before)" +
            "AND (:after IS NULL OR p.timestamp >= :after)" +
            "AND (:eventType IS NULL OR p.eventType = :eventType)")
    List<PasswordResetLog> findAllByCriteria(User user, LocalDateTime before, LocalDateTime after, PasswordEventType eventType, Pageable pageable);
}
