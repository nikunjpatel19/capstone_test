package tech.zodiac.px_um.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import tech.zodiac.px_um.dto.UpdateUserDto;
import tech.zodiac.px_um.dto.UserLoginDto;
import tech.zodiac.px_um.dto.UserRegistrationDto;
import tech.zodiac.px_um.model.User;
import tech.zodiac.px_um.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletRequest httpServletRequest;

    private User testUser;

    private Map<String, String> tokenMap;

    @BeforeEach
    void setUp() {
        testUser = new User("Test Test", "test@test.com", "testtest");
        testUser.setId(1);
        tokenMap = new HashMap<>();
        tokenMap.put("access_token", "access_token");
        tokenMap.put("refresh_token", "refresh_token");
    }

    @Test
    void testGetAllUsers() {
        List<User> users = List.of(testUser);

        when(userService.findUsersByCriteria(any(), any(), any())).thenReturn(users);

        ResponseEntity<List<User>> response = userController.getUsers(Optional.empty(), Optional.empty(), Optional.empty());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(users, response.getBody());
        assertEquals(testUser, response.getBody().get(0));
        verify(userService, times(1)).findUsersByCriteria(any(Optional.class), any(Optional.class), any(Optional.class));
    }

    @Test
    void testGetUserById() {
        when(userService.getUserById(1)).thenReturn(testUser);

        ResponseEntity<User> response = userController.getUserById(1);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUser, response.getBody());
        verify(userService, times(1)).getUserById(1);

    }

    @Test
    void testUpdateUser() {
        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setName("Updated Name");
        User updatedUser = new User("Updated Name", testUser.getEmail(), testUser.getPassword() );

        when(userService.updateUser(updateUserDto, 1, 1)).thenReturn(updatedUser);
        when(authentication.getName()).thenReturn(testUser.getId().toString());

        ResponseEntity<User> response = userController.updateUser(updateUserDto, authentication, 1);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedUser, response.getBody());
        verify(userService, times(1)).updateUser(updateUserDto, 1, 1);
    }

    @Test
    void testCreateUser() {
        UserRegistrationDto userRegistrationDto = new UserRegistrationDto(testUser.getName(), testUser.getEmail(), testUser.getPassword());

        when(userService.register(userRegistrationDto)).thenReturn(testUser);

        ResponseEntity<User> response = userController.createUser(userRegistrationDto);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testUser, response.getBody());
        verify(userService, times(1)).register(userRegistrationDto);
    }

    @Test
    void testLogin() {
        UserLoginDto loginDto  = new UserLoginDto();
        loginDto.setEmail(testUser.getEmail());
        loginDto.setPassword(testUser.getPassword());
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(userService.login(loginDto.getEmail(), loginDto.getPassword(), request, response)).thenReturn(tokenMap);

        ResponseEntity<Map<String, String>> responseEntity = userController.login(loginDto, request, response);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(tokenMap, responseEntity.getBody());
        verify(userService, times(1)).login(loginDto.getEmail(), loginDto.getPassword(), request, response);

    }

    @Test
    void testRefreshAccessToken() {
        Map<String, String> accessTokenMap = new HashMap<>();
        accessTokenMap.put("access_token", "access_token");

        when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer " + tokenMap.get("refresh_token"));
        when(userService.refreshAccessToken("refresh_token")).thenReturn(accessTokenMap);

        ResponseEntity<Map<String, String>> response = userController.refreshAccessToken(httpServletRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(accessTokenMap, response.getBody());
        verify(userService, times(1)).refreshAccessToken("refresh_token");

    }

    @Test
    void testLogout() {

        when(userService.logout("refresh_token")).thenReturn("Logout successful");
        when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer " + tokenMap.get("refresh_token"));

        ResponseEntity<String> response = userController.logout(httpServletRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logout successful", response.getBody());
        verify(userService, times(1)).logout("refresh_token");
    }

}
