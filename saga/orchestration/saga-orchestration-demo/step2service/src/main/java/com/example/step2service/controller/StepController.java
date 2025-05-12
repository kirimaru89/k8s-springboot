package com.example.step2service.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/step2")
public class StepController {

    private static final Logger log = LoggerFactory.getLogger(StepController.class);

    // Simulate a potential failure in step 2
    @Value("${simulate.failure:false}")
    private boolean simulateFailure;

    private int requestCount = 0;

    @PostMapping("/execute")
    public String executeStep(@RequestBody String input) {
        log.info("Step 2: Received execute request with input: {}", input);
        requestCount++;
        if (input.contains("fail2")) { 
            log.warn("Step 2: Simulating execution failure for input: {}", input);
            throw new RuntimeException("Step 2 simulated execution failure");
        }
        String result = "Step 2 processed: " + input;
        log.info("Step 2: Execution completed, returning: {}", result);
        return result;
    }

    @PostMapping("/reverse")
    public String reverseStep(@RequestBody String input) {
        if(input == null) {
            log.warn("Step 2: Received null input for reverse request");
        }
        log.info("Step 2: Received reverse request with input: {}", input);
        String result = "Step 2 reversed: " + input;
        log.info("Step 2: Reversal completed, returning: {}", result);
        return result;
    }
} 