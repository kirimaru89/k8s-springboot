package com.example.demo.controllers;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.services.App1Service;
import com.example.demo.services.AsyncApp1Service;
import com.example.demo.services.ReactiveApp1Service;
import com.example.demo.services.LoggingService;
import com.example.demo.services.BookService;
import com.example.demo.services.ExampleService;
import com.example.demo.services.CircuitBreakerCustomService;
import com.example.demo.dto.CircuitBreakerTestRequest;
import com.example.demo.dto.ApiResponse;

import com.example.demo.models.Book;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import com.example.demo.config.BodyFilterProperties;
import com.example.demo.config.DataSourceProperties;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/test")
public class TestController {
    @Autowired
    private DataSourceProperties dataSourceProps;

    @Autowired
    private BodyFilterProperties bodyFilterProps;

    @Autowired
    private ExampleService exampleService;

    @Autowired
    private CircuitBreakerCustomService circuitBreakerCustomService;

    @Autowired
    private BookService bookService;

    @Autowired
    private LoggingService loggingService;

    @Autowired
    private App1Service app1Service;

    @Autowired
    private ReactiveApp1Service reactiveApp1Service;

    @Autowired
    private AsyncApp1Service asyncApp1Service;

    @PostMapping("/sync")
    public String sync() {
//        log.info("Starting sync call to app1");
        String response = app1Service.getHelloFromApp1();
//        log.info("Completed sync call to app1 with response: {}", response);
        return "Response from app1: " + response;
    }

    @GetMapping("/call-app1-async")
    public CompletableFuture<String> callApp1Async() {
        loggingService.logInfo("before calling");

        return asyncApp1Service.callApp1Async()
                .thenApply(response -> {
                    loggingService.logInfo("Received async response from app1 then return---");
                    return "Async response from app1: " + response;
                });
    }

    @GetMapping("/call-app1-async-2")
    public CompletableFuture<String> callApp1Async2() {
        loggingService.logInfo("before calling");

        return asyncApp1Service.callApp1Async()
                .thenApply(response -> {
                    loggingService.logInfo("Received async response from app1 then return---");
                    return "Async response from app1: " + response;
                });
    }

    @GetMapping("/call-app1-async-error")
    public CompletableFuture<String> callApp1AsyncError() {
        return asyncApp1Service.callApp1AsyncError()
                .thenApply(response -> {
                    loggingService.logInfo("Received async response from app1 then return---");
                    return "Async response from app1: " + response;
                });
    }
    
    @GetMapping("/fire-forget")
    public String fireAndForget() {
        // Start the async task but don't wait for it
        // asyncApp1Service.fireAndForget();
        reactiveApp1Service.fireAndForgetReactive();
        // Return immediately
        return "Request initiated, not waiting for result";
    }

    @GetMapping("/get-book/{id}")
    public Optional<Book> getBookById(@PathVariable Long id) {
        return bookService.getBookById(id);
    }


    @GetMapping("/test-circuit-breaker/success")
    public ResponseEntity<ApiResponse<String>> testCircuitBreakerSuccess() {
        loggingService.logInfo("Calling circuit breaker success endpoint");
        return exampleService.serviceWithSuccessfulResponse();
    }

    @GetMapping("/test-circuit-breaker/failure")
    public ResponseEntity<ApiResponse<String>> testCircuitBreakerFailure() {
       return exampleService.serviceWithFailureResponse();
    }

    @GetMapping("/test-circuit-breaker/timeout")
    public CompletableFuture<ResponseEntity<ApiResponse<String>>> testCircuitBreakerTimeout() {
        return exampleService.serviceWithTimeout();
    }

    @GetMapping("/test-circuit-breaker/status")
    public ResponseEntity<ApiResponse<String>> getCircuitBreakerStatus() {
        return exampleService.getCircuitBreakerStatus();
    }

    @PostMapping("/test-circuit-breaker/custom")
    public ResponseEntity<ApiResponse<String>> testCircuitBreakerCustom(@RequestBody CircuitBreakerTestRequest request) {
        loggingService.logInfo("Testing circuit breaker for bank: " + request.getBankName()
            + ", operation: " + request.getOperation());
        return circuitBreakerCustomService.testWithCustomConfig(request);
    }

    @GetMapping("/test-circuit-breaker/custom/status/{bankName}")
    public ResponseEntity<ApiResponse<String>> getCustomCircuitBreakerStatus(@PathVariable String bankName) {
        loggingService.logInfo("Checking circuit breaker status for bank: " + bankName);
        return circuitBreakerCustomService.getCircuitBreakerStatus(bankName);
    }

    @GetMapping("/config-test")
    public Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("patternCount", bodyFilterProps.getPatterns().size());
        config.put("patterns", bodyFilterProps.getPatterns().stream()
            .map(p -> Map.of(
                "pattern", p.getPattern(),
                "replacement", p.getReplacement()
            ))
            .collect(Collectors.toList()));
        return config;
    }

    @GetMapping("/vault-test")
    public Map<String, Object> getVaultConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("url", dataSourceProps.getUrl());
        config.put("username", dataSourceProps.getUsername());
        config.put("password", dataSourceProps.getPassword());
        config.put("driverClassName", dataSourceProps.getDriverClassName());
        return config;
    }
}