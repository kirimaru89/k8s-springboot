package com.vietinbank.paymenthub.services;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.vietinbank.paymenthub.repositories.BookRepository;
import com.vietinbank.paymenthub.models.Book;
import java.util.List;

@Service
public class AsyncApp1Service {
    private final RestTemplate restTemplate;

    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    public AsyncApp1Service(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Async
    public CompletableFuture<String> callSpringApp2Async() {
        // print books
        List<Book> books = bookRepository.findAll();
        for (Book book : books) {
            System.out.println("Book: " + book.getTitle());
        }
        String result = restTemplate.getForObject("http://spring-app-2-service.default.svc.cluster.local:8080/api/test/call-async-flow-to-spring-app-3", String.class);
        return CompletableFuture.completedFuture(result);
    }
    
    @Async
    public CompletableFuture<String> callApp1Async() {
        String result = restTemplate.getForObject("http://spring-app-2-service.default.svc.cluster.local:8080/api/hello-async", String.class);
        return CompletableFuture.completedFuture(result);
    }

    @Async
    public CompletableFuture<String> callApp1AsyncError() {
        String result = restTemplate.getForObject("http://spring-app-2-service.default.svc.cluster.local:8080/api/hello-async-error", String.class);
        return CompletableFuture.completedFuture(result);
    }

    @Async
    public void fireAndForget() {
        // This runs in background, response is ignored
        restTemplate.getForObject("http://spring-app-2-service.default.svc.cluster.local:8080/api/hello", String.class);
        // Method returns void, no waiting
    }
}