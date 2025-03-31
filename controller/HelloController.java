package tech.zodiac.px_um.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String sayHello() {
        return "<h1 style='color: green;'>Hello from Spring Boot! </h1>";
    }
}