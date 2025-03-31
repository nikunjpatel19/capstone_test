package tech.zodiac.px_um.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import tech.zodiac.px_um.dto.UpdateUserDto;
import tech.zodiac.px_um.dto.UserLoginDto;
import tech.zodiac.px_um.dto.UserRegistrationDto;
import tech.zodiac.px_um.model.User;
import tech.zodiac.px_um.repository.RefreshTokenRepository;
import tech.zodiac.px_um.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @GetMapping
    public ResponseEntity<List<User>> getUsers(@RequestParam Optional<String> page,
                                               @RequestParam Optional<String> role,
                                               @RequestParam Optional<String> name) {
        return ResponseEntity.ok(userService.findUsersByCriteria(page, role, name));
    }

    //Get user by id
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    //Update user by id
    @PatchMapping("/{id}")
    public ResponseEntity<User> updateUser(@Valid @RequestBody UpdateUserDto user, Authentication authentication,@PathVariable Integer id) {
        User updatedUser = userService.updateUser(user, Integer.decode(authentication.getName()),id);
        return ResponseEntity.ok(updatedUser);

    }

    @PostMapping("/registration")
    public ResponseEntity<User> createUser(@Valid @RequestBody UserRegistrationDto user) {
        User newUser = userService.register(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @Valid @RequestBody UserLoginDto user,
            HttpServletRequest request,
            HttpServletResponse response
            ) {
        Map<String, String> tokenList = userService.login(user.getEmail(), user.getPassword(), request, response);
        return ResponseEntity.ok(tokenList);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshAccessToken(HttpServletRequest request) {
        String refreshToken = request.getHeader("Authorization").replace("Bearer ", "");
        if (refreshToken.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token is required");
        }
        Map<String, String> tokensList = userService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(tokensList);
    }

    //Able to enter with access token.
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, UsernamePasswordAuthenticationToken authentication) {
        String response = userService.logout(request.getHeader("Authorization").replace("Bearer ",  ""));
        return ResponseEntity.ok(response);
    }

}
