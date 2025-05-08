package com.example.step1service.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/step1") // Base path for this controller, can be adjusted if needed
public class StepController {

    private static final Logger log = LoggerFactory.getLogger(StepController.class);

    @PostMapping("/execute")
    public String executeStep(@RequestBody String input) {
        log.info("Step 1: Received execute request with input: {}", input);
        // Simulate processing
        String result = "Step 1 processed: " + input;
        log.info("Step 1: Execution completed, returning: {}", result);
        return result;
    }

    @PostMapping("/reverse")
    public String reverseStep(@RequestBody String input) {
        log.info("Step 1: Received reverse request with input: {}", input);
        // Simulate reversal
        String result = "Step 1 reversed: " + input;
        log.info("Step 1: Reversal completed, returning: {}", result);
        return result;
    }
} 