package tech.zodiac.px_um.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import tech.zodiac.px_um.dto.ChangePasswordDto;
import tech.zodiac.px_um.dto.ForgotPasswordDto;
import tech.zodiac.px_um.dto.ResetPasswordDto;
import tech.zodiac.px_um.service.PasswordService;
import tech.zodiac.px_um.service.UserService;

@RestController
public class PasswordController {

    @Autowired
    private PasswordService passwordService;
    @Autowired
    private UserService userService;

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordDto forgotPasswordDto) {
        return ResponseEntity.ok(passwordService.forgotPassword(forgotPasswordDto.getEmail()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody ResetPasswordDto resetPasswordDto) {
        String resetToken = authorizationHeader.replace("Bearer ", "");
        passwordService.resetPassword(resetToken, resetPasswordDto.getNewPassword());
        return ResponseEntity.ok("Password successfully reset");
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordDto changePasswordDto,
            Authentication authentication
    ){
        Integer updateUserID = Integer.valueOf(authentication.getName());
        userService.changePassword(updateUserID, changePasswordDto.getCurrent_password(), changePasswordDto.getNew_password());
        return ResponseEntity.ok("Password update successfully ");

    }
}
