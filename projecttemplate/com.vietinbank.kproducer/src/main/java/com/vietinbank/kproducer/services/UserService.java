package com.vietinbank.kproducer.services;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.vietinbank.kproducer.dto.request.user.LoginRequestDto;
import com.vietinbank.kproducer.dto.request.user.RegisterRequestDto;
import com.vietinbank.kproducer.dto.response.user.LoginResponseDto;
import com.vietinbank.kproducer.models.User;
import com.vietinbank.kproducer.repositories.UserRepository;
import com.vietinbank.kproducer.security.JwtUtil;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(
            UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public ResponseEntity<Void> register(RegisterRequestDto request) {
        boolean checkUsernameExists = userRepository.existsByUsername(request.getUsername());
        if (checkUsernameExists) {
            return ResponseEntity.badRequest().build();
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<LoginResponseDto> login(LoginRequestDto request) {
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = jwtUtil.generateToken(user.getUsername());

        return ResponseEntity.ok(new LoginResponseDto(token));
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return null;
    }

    public User getCurrentUser() {
        String username = getCurrentUsername();
        if (username != null) {
            return userRepository.findByUsername(username).orElseThrow();
        }
        return null;
    }

    public UserDetails getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userDetails;
        }
        return null;
    }
}
