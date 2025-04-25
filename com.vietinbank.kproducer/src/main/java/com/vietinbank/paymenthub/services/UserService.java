package com.vietinbank.paymenthub.services;

import com.vietinbank.paymenthub.dto.response.ApiResponseDto;
import com.vietinbank.paymenthub.common.ResponseCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.vietinbank.paymenthub.dto.request.user.LoginRequestDto;
import com.vietinbank.paymenthub.dto.request.user.RegisterRequestDto;
import com.vietinbank.paymenthub.dto.response.user.LoginResponseDto;
import com.vietinbank.paymenthub.models.User;
import com.vietinbank.paymenthub.repositories.UserRepository;
import com.vietinbank.paymenthub.security.JwtUtil;

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

    public ResponseEntity<ApiResponseDto<Void>> register(RegisterRequestDto request) {
        boolean checkUsernameExists = userRepository.existsByUsername(request.getUsername());
        if (checkUsernameExists) {
            return ApiResponseDto.error(ResponseCode.BAD_REQUEST, String.format("Username %s already exists", request.getUsername()));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return ApiResponseDto.success(null);
    }

    public ResponseEntity<ApiResponseDto<LoginResponseDto>> login(LoginRequestDto request) {
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);
        if (user == null) {
            return ApiResponseDto.error(ResponseCode.BAD_REQUEST, String.format("Username %s not exists", request.getUsername()));
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ApiResponseDto.error(ResponseCode.UNAUTHORIZED, "Incorrect password");
        }
        String token = jwtUtil.generateToken(user.getUsername());

        return ApiResponseDto.success(new LoginResponseDto(token));
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
