package tech.zodiac.px_um.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tech.zodiac.px_um.dto.UpdateUserDto;
import tech.zodiac.px_um.dto.UserRegistrationDto;
import tech.zodiac.px_um.exception.*;
import tech.zodiac.px_um.model.*;
import tech.zodiac.px_um.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private UserActionLogService userActionLogService;

    @Autowired
    private PasswordResetLogService passwordResetLogService;

    private final Integer USERS_PER_PAGE = 5;

    public User register(UserRegistrationDto user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User with the email " + user.getEmail() + " already exists.");
        }
        User userEntity = new User(
                user.getName(),
                user.getEmail(),
                passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(userEntity);

        userActionLogService.addUserActionLog(savedUser, ActionType.USER_CREATION, Status.SUCCESS, "User Registered", null);

        return savedUser;
    }

    public List<User> findUsersByCriteria(Optional<String> page, Optional<String> roleOptional, Optional<String> nameOptional) {
        Pageable pageable;

        if (page.isPresent()) {
            String pageString = page.get();
            if (pageString.equalsIgnoreCase("all"))
                pageable = Pageable.unpaged();
            else {
                int pageNumber = Integer.parseInt(pageString) - 1;
                pageable = PageRequest.of(pageNumber, USERS_PER_PAGE);
            }
        } else {
            pageable = PageRequest.of(0, USERS_PER_PAGE);
        }

        Role role = roleOptional.map(s -> Role.valueOf(s.toUpperCase())).orElse(null);
        String name = nameOptional.orElse(null);

        return userRepository.findAllByRoleAndName(role, name, pageable);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User getUserById(Integer userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public User updateUser(UpdateUserDto updateUserDto, Integer actionUserId, Integer targetUserId) {
        StringBuilder detailMessage = new StringBuilder();

        User storedUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!storedUser.getId().equals(actionUserId)) {
            String errorMessage = "Update Other User detail is forbidden";

            userActionLogService.addUserActionLog(storedUser, ActionType.PROFILE_UPDATE, Status.FAILURE, errorMessage, null);

            throw new UserAccessDeniedException(errorMessage);
        }

        Optional.ofNullable(updateUserDto.getName()).ifPresent(name -> {
            if (name.isBlank())
                throw new ValidationException("Name cannot be blank");
            else {
                detailMessage.append("username from '" + storedUser.getName() + "' to '" + name+ "' ");
                storedUser.setName(name);
            }
        });
        Optional.ofNullable(updateUserDto.getEmail()).ifPresent(email -> {
                    if (email.isBlank())
                        throw new ValidationException("Email cannot be blank");
                    else {
                        detailMessage.append(", email from '" + storedUser.getEmail() + "' to '" + email + "' ");
                        storedUser.setEmail(email);
                    }
                }
        );

        User savedUser = userRepository.save(storedUser);

        userActionLogService.addUserActionLog(savedUser, ActionType.PROFILE_UPDATE, Status.SUCCESS, detailMessage.toString(), null);

        return savedUser;
    }

    //will return both access and refresh tokens
    public Map<String, String> login(String email, String password, HttpServletRequest request, HttpServletResponse response) {
        String detailMessage;
        String remoteAddr = request.getRemoteAddr();

        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User Not Found"));
        if (!user.isActive()) {
            detailMessage = "User is Deactivated";

            userActionLogService.addUserActionLog(user, ActionType.LOGIN, Status.FAILURE, "reason: " + detailMessage, remoteAddr);

            throw new UserAccessDeniedException(detailMessage);
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            detailMessage = "Invalid Password";

            userActionLogService.addUserActionLog(user, ActionType.LOGIN, Status.FAILURE, "reason: " + detailMessage, remoteAddr);
            throw new InvalidCredentialsException(detailMessage);
        }
        Map<String, String> tokenMap = new HashMap<>(Map.of(
                "access_token", jwtService.generateAccessToken(user.getId()),
                "refresh_token", jwtService.generateRefreshToken(user.getId())
        ));

        detailMessage = "Login successful";
        userActionLogService.addUserActionLog(user, ActionType.LOGIN, Status.SUCCESS, detailMessage, remoteAddr);

        return tokenMap;
    }

    //will only return the access token, as the refresh token is only used to generate a new access token
    public Map<String, String> refreshAccessToken(String refreshToken) {
        Integer userId = jwtService.extractUserId(refreshToken);
        if (!jwtService.validateRefreshToken(refreshToken, userId)) {
            throw new InvalidTokenException("Invalid refresh token");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
        String accessToken = jwtService.generateAccessToken(user.getId());
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("access_token", accessToken);
        return tokenMap;
    }

    public void deactivateUserById(Integer userId, Integer actionUserId) {
        User actionUser = userRepository.findById(actionUserId).orElseThrow(() -> new UserNotFoundException("Action User not found"));
        User deactivateUser = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Target User not found"));
        if (actionUser.getRole() == Role.ADMIN) {
            if (!deactivateUser.isActive()) {
                throw new UserAccessDeniedException("User is already deactivated");
            }
            deactivateUser.setActive(false);
            userRepository.save(deactivateUser);
        } else {
            throw new UserAccessDeniedException("NonAuthorized Access");
        }
    }

    public void activateUserById(Integer userId, Integer actionUserId) {
        User actionUser = userRepository.findById(actionUserId).orElseThrow(() -> new UserNotFoundException("Action User not found"));
        User activateUser = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Target User not found"));
        if (actionUser.getRole() == Role.ADMIN) {
            if (activateUser.isActive()) {
                throw new UserAccessDeniedException("User is already activated");

            }
            activateUser.setActive(true);
            userRepository.save(activateUser);
        } else {
            throw new UserAccessDeniedException("NonAuthorized Access");
        }
    }

    public void changePassword(Integer userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new PasswordException("Incorrect current password");
        }

        if (!isValidPassword(newPassword)) {
            throw new PasswordException("New password does not meet security requirements");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetLogService.addPasswordResetLog(user, PasswordEventType.PASSWORD_CHANGED);

        jwtService.deleteRefreshTokenByUserId(userId);
    }

    public boolean isValidPassword(String password) {
        return password.length() >= 8;
    }

    public String logout(String refreshToken) {
        jwtService.deleteRefreshToken(refreshToken);
        return "Logout successful";
    }
}
