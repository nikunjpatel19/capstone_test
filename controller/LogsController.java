package tech.zodiac.px_um.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.zodiac.px_um.model.LogType;
import tech.zodiac.px_um.service.LogService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/logs")
public class LogsController {

    private final LogService logService;

    public LogsController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping("users")
    public ResponseEntity<List<?>> getUserLogs(@RequestParam LogType type,
                                               @RequestParam Optional<Integer> userId,
                                               @RequestParam Optional<String> event,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> before,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> after,
                                               @RequestParam Optional<String> status,
                                               @RequestParam Optional<String> ip,
                                               @RequestParam Optional<String> email,
                                               @RequestParam Optional<String> page
                                               ) {
        return ResponseEntity.ok().body(logService.getUserActionLogs(type, userId, event, before,
                                                                    after, status, ip, email, page));
    }
}
