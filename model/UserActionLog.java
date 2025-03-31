package tech.zodiac.px_um.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "user_audit_log")
public class UserActionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Enumerated
    private ActionType actionType;
    private LocalDateTime timestamp;
    @Enumerated
    private Status status;
    @Column(columnDefinition = "TEXT")
    private String details;
    private String ipAddress;

}
