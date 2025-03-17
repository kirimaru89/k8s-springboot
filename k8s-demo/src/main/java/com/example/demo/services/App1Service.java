package com.example.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class App1Service {

    private final RestTemplate restTemplate;
    
    @Autowired
    public App1Service(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public String getHelloFromApp1() {
        // The service name "app1" is used as the hostname within Docker network
        return restTemplate.getForObject("http://spring-app-1:8081/api/hello", String.class);
    }
}