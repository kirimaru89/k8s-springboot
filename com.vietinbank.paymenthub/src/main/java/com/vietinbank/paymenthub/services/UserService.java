package com.vietinbank.paymenthub.services;

import com.vietinbank.paymenthub.dto.RegisterRequest;
import com.vietinbank.paymenthub.models.User;
import com.vietinbank.paymenthub.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private final Counter registrationCounter;
    private final Counter registrationSuccessCounter;
    private final Counter registrationFailureCounter;
    private final Timer registrationTimer;
    
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, MeterRegistry registry) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        
        // Initialize counters and timers
        this.registrationCounter = Counter.builder("app.users.registration.attempts")
            .description("Total number of user registration attempts")
            .register(registry);
            
        this.registrationSuccessCounter = Counter.builder("app.users.registration.success")
            .description("Number of successful user registrations")
            .register(registry);
            
        this.registrationFailureCounter = Counter.builder("app.users.registration.failures")
            .description("Number of failed user registrations")
            .register(registry);
            
        this.registrationTimer = Timer.builder("app.users.registration.duration")
            .description("Time taken to register a new user")
            .register(registry);
    }

    public String register(RegisterRequest request) {
        // Increment the registration attempt counter
        registrationCounter.increment();
        
        // Use timer to measure registration duration
        return registrationTimer.record(() -> {
            try {
                User user = new User();
                user.setUsername(request.getUsername());
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                userRepository.save(user);
                
                // Increment success counter
                registrationSuccessCounter.increment();
                return "User registered successfully!";
            } catch (Exception e) {
                // Increment failure counter
                registrationFailureCounter.increment();
                throw e;
            }
        });
    }
}
