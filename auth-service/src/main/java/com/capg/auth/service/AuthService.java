package com.capg.auth.service;

import com.capg.auth.dto.*;
import com.capg.auth.entity.User;
import com.capg.auth.exception.InvalidCredentialsException;
import com.capg.auth.exception.UserAlreadyExistsException;
import com.capg.auth.exception.UserNotFoundException;
import com.capg.auth.repository.AuthRepository;
import com.capg.auth.util.JwtUtil;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService {

    private final AuthRepository repo;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder encoder;

    public AuthService(AuthRepository repo, JwtUtil jwtUtil, PasswordEncoder encoder) {
        this.repo = repo;
        this.jwtUtil = jwtUtil;
        this.encoder = encoder;
    }

    public void signup(SignupRequest dto) {

        if (repo.findByEmail(dto.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists");
        }
        
        if (repo.findByUsername(dto.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(encoder.encode(dto.getPassword()));
        String role = dto.getRole();
        if (role == null || role.trim().isEmpty()) {
            role = "USER";
        }
        user.setRole(role.toUpperCase());

        log.info("Registering new user: {} with role: {}", user.getUsername(), user.getRole());
        repo.save(user);
    }

    public Response login(LoginRequest dto) {

        User user = repo.findByUsername(dto.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!encoder.matches(dto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        log.info("User {} logged in successfully", user.getUsername());
        String token = jwtUtil.generateToken(user.getEmail(), user.getUsername(), user.getRole());

        return new Response(token);

    }
    
    public List<User> getAllUsers() {
        return repo.findAll();
    }
}