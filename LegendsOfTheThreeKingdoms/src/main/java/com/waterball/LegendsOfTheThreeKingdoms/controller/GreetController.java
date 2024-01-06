package com.waterball.LegendsOfTheThreeKingdoms.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetController {

    @GetMapping("/greet")
    public String greet() {
        return "Hello 三國殺 From EC2 20240107-1";
    }
}
