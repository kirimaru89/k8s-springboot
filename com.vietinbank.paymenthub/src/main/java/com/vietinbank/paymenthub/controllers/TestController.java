package com.vietinbank.paymenthub.controllers;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.micrometer.core.annotation.Timed;

import com.vietinbank.paymenthub.services.App1Service;
import com.vietinbank.paymenthub.services.AsyncApp1Service;
import com.vietinbank.paymenthub.services.ReactiveApp1Service;
import com.vietinbank.paymenthub.services.LoggingService;
import com.vietinbank.paymenthub.services.BookService;

import com.vietinbank.paymenthub.models.Book;
import java.util.Optional;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/test")
public class TestController {
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
        String response = app1Service.getHelloFromApp1();
        return "Response from app1: " + response;
    }
    
    @Timed(value = "spring-app-1.test.call-async-flow-to-spring-app-2", description = "Time spent in service call")
    @GetMapping("/call-async-flow-to-spring-app-2/{id}")
    public CompletableFuture<String> callAsyncFlowToSpringApp2(@PathVariable Long id) {
        Optional<Book> book = bookService.getBookById(id);
        if (book.isPresent()) {
            loggingService.logInfo("Book: " + book.get().getTitle());
        } else {
            loggingService.logInfo("Book not found");
        }

        loggingService.logInfo("before calling");

        return asyncApp1Service.callSpringApp2Async()
                .thenApply(response -> {
                    loggingService.logInfo("Received async response from spring-app-2 then return---");
                    return "Async response from spring-app-2: " + response;
                });
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
}