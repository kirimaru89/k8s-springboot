package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "datasource")
@Getter
@Setter
public class MyDataSourceProperties {
    private String username;
    private String password;

    @PostConstruct
    public void print() {
        System.out.println("✅ Vault injected config -> username: " + username);
        System.out.println("✅ Vault injected config -> password: " + password);
    }
}