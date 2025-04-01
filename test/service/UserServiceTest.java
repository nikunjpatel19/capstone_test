package tech.zodiac.px_um.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import tech.zodiac.px_um.dto.UpdateUserDto;
import tech.zodiac.px_um.dto.UserRegistrationDto;
import tech.zodiac.px_um.exception.*;
import tech.zodiac.px_um.model.*;
import tech.zodiac.px_um.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserActionLogService userActionLogService;

    @Mock
    private PasswordResetLogService passwordResetLogService;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("Test User", "test@example.com", "encodedPassword");
        testUser.setId(1);
        testUser.setActive(true);
    }

    @Test
    void register_NewUser_Success() {
        UserRegistrationDto dto = new UserRegistrationDto("New User", "new@example.com", "password");
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User savedUser = userService.register(dto);

        assertNotNull(savedUser);
        assertEquals("Test User", savedUser.getName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_UserAlreadyExists_ThrowsException() {
        UserRegistrationDto dto = new UserRegistrationDto("New User", "test@example.com", "password");
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(testUser));

        assertThrows(UserAlreadyExistsException.class, () -> userService.register(dto));
    }

    @Test
    void login_ValidCredentials_Success() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", testUser.getPassword())).thenReturn(true);
        when(jwtService.generateAccessToken(anyInt())).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(anyInt())).thenReturn("refreshToken");

        Map<String, String> tokens = userService.login("test@example.com", "password", request, response);

        assertNotNull(tokens);
        assertEquals("accessToken", tokens.get("access_token"));
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", testUser.getPassword())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> userService.login("test@example.com", "wrongPassword", request, mock(HttpServletResponse.class)));
    }

    @Test
    void updateUser_ValidUpdate_Success() {
        UpdateUserDto dto = new UpdateUserDto();
        dto.setName("Updated Name");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User updatedUser = userService.updateUser(dto, 1, 1);

        assertNotNull(updatedUser);
        assertEquals("Updated Name", updatedUser.getName());
    }

    @Test
    void updateUser_ForbiddenUpdate_ThrowsException() {
        UpdateUserDto dto = new UpdateUserDto();
        dto.setName("Updated Name");

        when(userRepository.findById(2)).thenReturn(Optional.of(testUser));

        assertThrows(UserAccessDeniedException.class, () -> userService.updateUser(dto, 3, 2));
    }

    @Test
    void refreshAccessToken_ValidToken_Success() {
        when(jwtService.extractUserId("validToken")).thenReturn(1);
        when(jwtService.validateRefreshToken("validToken", 1)).thenReturn(true);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(jwtService.generateAccessToken(1)).thenReturn("newAccessToken");

        Map<String, String> tokens = userService.refreshAccessToken("validToken");

        assertEquals("newAccessToken", tokens.get("access_token"));
    }

    @Test
    void refreshAccessToken_InvalidToken_ThrowsException() {
        String refreshToken = "invalidToken";
        int userId = 1;

        when(jwtService.extractUserId(refreshToken)).thenReturn(userId);
        when(jwtService.validateRefreshToken(eq(refreshToken), anyInt())).thenReturn(false);

        assertThrows(InvalidTokenException.class, () -> userService.refreshAccessToken(refreshToken));

        verify(jwtService).extractUserId(refreshToken);
        verify(jwtService).validateRefreshToken(refreshToken, userId);
        verifyNoMoreInteractions(userRepository, jwtService);
    }


    @Test
    void deactivateUserById_Admin_Success() {
        User admin = new User("Admin", "admin@example.com", "password");
        admin.setId(2);
        admin.setRole(Role.ADMIN);
        when(userRepository.findById(2)).thenReturn(Optional.of(admin));
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.deactivateUserById(1, 2);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void changePassword_ValidChange_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("currentPassword", testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        userService.changePassword(1, "currentPassword", "newPassword");

        verify(userRepository).save(any(User.class));
        verify(passwordResetLogService).addPasswordResetLog(any(User.class), any(PasswordEventType.class));
    }
}
