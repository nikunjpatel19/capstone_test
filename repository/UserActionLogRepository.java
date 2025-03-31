package tech.zodiac.px_um.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tech.zodiac.px_um.model.*;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActionLogRepository extends JpaRepository<UserActionLog, Integer> {

    @Query("SELECT u FROM UserActionLog u "+
            "WHERE (:user IS NULL OR u.user = :user) " +
            "AND (:before IS NULL OR u.timestamp <= :before)" +
            "AND (:after IS NULL OR u.timestamp >= :after)" +
            "AND (:ipAddress IS NULL OR u.ipAddress = :ipAddress)" +
            "AND (:status IS NULL OR u.status = :status)" +
            "AND (:actionType IS NULL OR u.actionType = :actionType)")
    List<UserActionLog> findAllByCriteria(User user, LocalDateTime before, LocalDateTime after, String ipAddress,
                                                 ActionType actionType, Status status, Pageable pageable);

}