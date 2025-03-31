package tech.zodiac.px_um.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tech.zodiac.px_um.exception.InvalidTokenException;
import tech.zodiac.px_um.exception.UserNotFoundException;
import tech.zodiac.px_um.model.EmailEventType;
import tech.zodiac.px_um.model.User;
import tech.zodiac.px_um.model.VerificationToken;
import tech.zodiac.px_um.repository.UserRepository;
import tech.zodiac.px_um.repository.VerificationTokenRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class VerificationService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    HashingService hashingService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    VerificationTokenRepository verificationTokenRepository;

    @Autowired
    EmailVerificationLogService emailVerificationLogService;

    private final Integer EXPIRES_IN_HOURS = 1;
    private final String FRONT_END_URL;


    public VerificationService(@Value("${front-end-url}") String frontEndUrl) {
        FRONT_END_URL = frontEndUrl;
    }

    public String generateVerificationToken(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Optional<VerificationToken> storedToken = verificationTokenRepository.findByUser(user);
            storedToken.ifPresent(verificationTokenRepository::delete);

            VerificationToken verificationToken = new VerificationToken();
            verificationToken.setUser(user);
            String token = UUID.randomUUID().toString();
            verificationToken.setToken(hashingService.hash(token));
            verificationToken.setExpiresTime(LocalDateTime.now().plusHours(EXPIRES_IN_HOURS));
            verificationTokenRepository.save(verificationToken);
            String url = FRONT_END_URL + "/verify-user?token=" + token;

            emailVerificationLogService.addEmailVerificationLog(user, EmailEventType.SENT, email);

            return url;
        } else {
            throw new UserNotFoundException("User not found");
        }
    }

    public String resendVerificationToken(Integer userID) {
        Optional<User> userOptional = userRepository.findById(userID);
        if (userOptional.isPresent()) {
            return generateVerificationToken(userOptional.get().getEmail());
        }
        else {
            throw new UserNotFoundException("User not found");
        }
    }

    public void verifyUser(String token) {
        Optional<VerificationToken> verificationToken = verificationTokenRepository.findBytoken(hashingService.hash(token));
        if (verificationToken.isPresent()) {
            if (verificationToken.get().getExpiresTime().isBefore(LocalDateTime.now())) {
                verificationTokenRepository.delete(verificationToken.get());
                throw new InvalidTokenException("Token has expired");
            }
            User user = verificationToken.get().getUser();
            user.setVerified(true);
            userRepository.save(user);

            emailVerificationLogService.addEmailVerificationLog(user, EmailEventType.VERIFIED, null);

            verificationTokenRepository.delete(verificationToken.get());
        } else {
            throw new InvalidTokenException("Token not found");
        }
    }
}
