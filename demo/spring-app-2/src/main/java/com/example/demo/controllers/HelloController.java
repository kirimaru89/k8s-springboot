package com.example.demo.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    
    @GetMapping("/api/hello")
    public String hello() {
        return "Hello World";
    }

    @GetMapping("/api/hello-async")
    public String helloAsync() {
        return "Hello World Async";
    }

    @GetMapping("/api/hello-async-error")
    public String helloAsyncError() {
        throw new RuntimeException("Hello World Async Error");
    }
}