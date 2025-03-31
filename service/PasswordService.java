package tech.zodiac.px_um.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tech.zodiac.px_um.exception.InvalidTokenException;
import tech.zodiac.px_um.exception.PasswordException;
import tech.zodiac.px_um.exception.UserNotFoundException;
import tech.zodiac.px_um.model.EmailEventType;
import tech.zodiac.px_um.model.PasswordEventType;
import tech.zodiac.px_um.model.ResetPasswordToken;
import tech.zodiac.px_um.model.User;
import tech.zodiac.px_um.repository.RefreshTokenRepository;
import tech.zodiac.px_um.repository.ResetPasswordTokenRepository;
import tech.zodiac.px_um.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordService {

    @Autowired
    ResetPasswordTokenRepository resetPasswordTokenRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    HashingService hashingService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    PasswordResetLogService passwordResetLogService;

    @Autowired
    EmailVerificationLogService emailVerificationLogService;

    private final Integer EXPIRES_IN_HOURS = 1;
    private final String FRONT_END_URL;

    public PasswordService(@Value("${front-end-url}") String frontEndUrl) {
        FRONT_END_URL = frontEndUrl;
    }

    public String forgotPassword(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Optional<ResetPasswordToken> storedToken = resetPasswordTokenRepository.findByUser(user);
            storedToken.ifPresent(resetPasswordTokenRepository::delete);

            ResetPasswordToken resetPasswordToken = new ResetPasswordToken();
            String token = UUID.randomUUID().toString();
            resetPasswordToken.setResetPasswordToken(hashingService.hash(token));
            resetPasswordToken.setUser(user);
            resetPasswordToken.setExpiresTime(LocalDateTime.now().plusHours(EXPIRES_IN_HOURS));
            resetPasswordTokenRepository.save(resetPasswordToken);

            String url = FRONT_END_URL + "/reset-password?token=" + token;

            passwordResetLogService.addPasswordResetLog(user, PasswordEventType.URL_CREATED);
            emailVerificationLogService.addEmailVerificationLog(user, EmailEventType.SENT, email);

            return url;
        } else {
            throw new UserNotFoundException("user email " + email +" not found");
        }
    }

    public void resetPassword(String resetToken, String newPassword) {
        Optional<ResetPasswordToken> resetTokenOptional = resetPasswordTokenRepository.findByResetPasswordToken(hashingService.hash(resetToken));

        if (resetTokenOptional.isEmpty()) {
            throw new InvalidTokenException("Invalid token");
        }

        ResetPasswordToken resetPasswordToken = resetTokenOptional.get();

        if (resetPasswordToken.getExpiresTime().isBefore(LocalDateTime.now())) {
            resetPasswordTokenRepository.delete(resetPasswordToken);
            throw new InvalidTokenException("Token has expired");
        }

        if (!userService.isValidPassword(newPassword)) {
            throw new PasswordException("New password does not meet security requirements");
        }

        User user = resetPasswordToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetPasswordTokenRepository.delete(resetPasswordToken);

        passwordResetLogService.addPasswordResetLog(user, PasswordEventType.PASSWORD_CHANGED);

        // Delete all refresh tokens to log out all sessions
        refreshTokenRepository.deleteAllInBatch(refreshTokenRepository.findAllByUser(user));
    }
}
