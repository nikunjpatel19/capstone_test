package tech.zodiac.px_um.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import tech.zodiac.px_um.service.HashingService;
import tech.zodiac.px_um.service.Sha256HashingService;

import java.util.Set;

@Configuration
public class AppConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HashingService hashingService() {
        return new Sha256HashingService();
    }

    @Bean
    public Set<String> excludedEndpoints() {
        return Set.of(
                "/users/login",
                "/users/registration",
                "/forgot-password",
                "/reset-password",
                "/verify-link",
                "/verify-user",
                "/swagger-ui/**",
                "/api-docs/**"
        );
    }

    @Bean
    public Set<String> refreshEndpoints() {
        return Set.of(
                "/users/logout",
                "/users/refresh"
        );
    }

    @Bean
    public Set<String> adminEndpoints() {
        return Set.of(
                "/logs/**"
        );
    }
}
