package com.example.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.concurrent.CompletableFuture;
import com.example.demo.repositories.ArtistRepository;
import com.example.demo.models.Artist;
import java.util.List;

@Service
public class AsyncApp1Service {
    private final RestTemplate restTemplate;

    private final ArtistRepository artistRepository;
    
    @Autowired
    public AsyncApp1Service(RestTemplate restTemplate, ArtistRepository artistRepository) {
        this.restTemplate = restTemplate;
        this.artistRepository = artistRepository;
    }

    @Async
    public CompletableFuture<String> callSpringApp3Async() {
        List<Artist> artists = artistRepository.findAll();
        for (Artist artist : artists) {
            System.out.println("Artist: " + artist.getName());
        }
        String result = restTemplate.getForObject("http://spring-app-3-service.default.svc.cluster.local:8080/api/test/call-async-flow-to-spring-app-4", String.class);
        return CompletableFuture.completedFuture(result);
    }
    
    @Async
    public CompletableFuture<String> callApp1Async() {
        String result = restTemplate.getForObject("http://spring-app-1:8081/api/hello-async", String.class);
        return CompletableFuture.completedFuture(result);
    }

    @Async
    public CompletableFuture<String> callApp1AsyncError() {
        String result = restTemplate.getForObject("http://spring-app-1:8081/api/hello-async-error", String.class);
        return CompletableFuture.completedFuture(result);
    }

    @Async
    public void fireAndForget() {
        // This runs in background, response is ignored
        restTemplate.getForObject("http://spring-app-1:8081/api/hello", String.class);
        // Method returns void, no waiting
    }
}