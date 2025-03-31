package tech.zodiac.px_um.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.zodiac.px_um.model.EmailVerificationLog;
import tech.zodiac.px_um.model.EmailEventType;
import tech.zodiac.px_um.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmailVerificationLogRepository extends JpaRepository<EmailVerificationLog,Integer> {

    @Query("SELECT e FROM EmailVerificationLog e "+
            "WHERE (:user IS NULL OR e.user = :user) " +
            "AND (:before IS NULL OR e.timestamp <= :before)" +
            "AND (:after IS NULL OR e.timestamp >= :after)" +
            "AND (:email IS NULL OR e.email = :email)" +
            "AND (:eventType IS NULL OR e.eventType = :eventType)")
    List<EmailVerificationLog> findAllByCriteria(User user, LocalDateTime before, LocalDateTime after, String email, EmailEventType eventType, Pageable pageable);

}
