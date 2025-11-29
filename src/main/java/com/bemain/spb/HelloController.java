package com.bemain.spb;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/test")
    public String test() {
        return "Spring Boot Setup Success! 고생하셨습니다!";
    }
}