package com.example.step3service.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/step3")
public class StepController {

    private static final Logger log = LoggerFactory.getLogger(StepController.class);

    @Value("${simulate.failure:false}")
    private boolean simulateFailure;
    private int requestCount = 0;

    @PostMapping("/execute")
    public String executeStep(@RequestBody String input) {
        log.info("Step 3: Received execute request with input: {}", input);
        requestCount++;
        if (input.contains("fail")) { 
            log.warn("Step 3: Simulating execution failure for input: {}", input);
            throw new RuntimeException("Step 3 simulated execution failure");
        }
        String result = "Step 3 processed: " + input;
        log.info("Step 3: Execution completed, returning: {}", result);
        return result;
    }

    @PostMapping("/reverse")
    public String reverseStep(@RequestBody String input) {
        log.info("Step 3: Received reverse request with input: {}", input);
        String result = "Step 3 reversed: " + input;
        log.info("Step 3: Reversal completed, returning: {}", result);
        return result;
    }
} 