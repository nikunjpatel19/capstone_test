package tech.zodiac.px_um.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @CrossOrigin(origins = {"http://184.107.4.32:5173", "http://localhost:5173"}) // for server and local dev
    @GetMapping("/api/spring-test")
    public String testGet() {
        return "{\"data\":\"ok!\"}";
    }
}