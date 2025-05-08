package com.example.servicea.controller;

import com.example.servicea.service.GreetingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private static final Logger log = LoggerFactory.getLogger(HelloController.class);
    private final GreetingService greetingService;

    public HelloController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @GetMapping("/hello/{name}")
    public String hello(@PathVariable String name) {
        log.info("Received request for /hello/{}", name);
        String response = greetingService.greet(name);
        log.info("Responding with: {}", response);
        return response;
    }
} 