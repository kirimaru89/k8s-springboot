package com.example.demo.controllers;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.services.App1Service;
import com.example.demo.services.AsyncApp1Service;
import com.example.demo.services.ReactiveApp1Service;
import com.example.demo.services.LoggingService;
import com.example.demo.services.ArtistService;
import com.example.demo.models.Artist;
import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestController {
    @Autowired
    private ArtistService artistService;

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

    @GetMapping("/call-async-flow-to-spring-app-3")
    public CompletableFuture<String> callAsyncFlowToSpringApp3() {
        loggingService.logInfo("before calling");

        return asyncApp1Service.callSpringApp3Async()
                .thenApply(response -> {
                    loggingService.logInfo("Received async response from spring-app-3 then return---");
                    return "Async response from spring-app-3: " + response;
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
        // reactiveApp1Service.fireAndForgetReactive();

        List<Artist> artists = artistService.findAllArtists();
        for (Artist artist : artists) {
            loggingService.logInfo("Artist: " + artist.getName());
        }
        // Return immediately
        return "Request initiated, not waiting for result";
    }

    // @GetMapping("/test")
    // public String test() {
    //     logger.debug("Debug log message");
    //     logger.info("Info log message");
    //     logger.warn("Warning log message");
    //     logger.error("Error log message");
    //     return "Test logging";
    // }
}
