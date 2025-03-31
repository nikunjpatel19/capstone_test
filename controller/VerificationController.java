package tech.zodiac.px_um.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tech.zodiac.px_um.dto.VerificationDto;
import tech.zodiac.px_um.service.VerificationService;

@RestController
public class VerificationController {

    @Autowired
    private VerificationService verificationService;

    @PostMapping("/verify-link")
    public ResponseEntity<String> getVerifyLink(@Valid @RequestBody VerificationDto verificationDto) {
        return ResponseEntity.ok(verificationService.generateVerificationToken(verificationDto.getEmail()));
    }

    @PostMapping("/resend-verify-link")
    public ResponseEntity<String> resendVerify(Authentication authentication) {
        return ResponseEntity.ok(verificationService.resendVerificationToken(Integer.decode(authentication.getName())));
    }

    @PostMapping("/verify-user")
    public ResponseEntity<String> verifyLink(@RequestHeader("Authorization") String authorizationHeader) {
        String verifyToken = authorizationHeader.replace("Bearer ", "");
        verificationService.verifyUser(verifyToken);
        return ResponseEntity.ok("User successfully verified");
    }
}
