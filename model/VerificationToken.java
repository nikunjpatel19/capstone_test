package tech.zodiac.px_um.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Entity
@Getter
@Setter
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false,unique = true)
    private String token;
    @Column(name = "expires_time", nullable = false)
    private LocalDateTime expiresTime;

    public VerificationToken() {

    }
    public VerificationToken(User user, String token, LocalDateTime expiresTime) {
        this.user = user;
        this.token = token;
        this.expiresTime = expiresTime;
    }
}
