package com.capg.auth.controller;

import com.capg.auth.dto.Response;
import com.capg.auth.dto.LoginRequest;
import com.capg.auth.dto.SignupRequest;
import com.capg.auth.dto.UserDto;
import com.capg.auth.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@EnableMethodSecurity
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/signup")
    public String signup(@Valid @RequestBody SignupRequest dto) {
        service.signup(dto);
        return "User registered successfully";
    }

    @PostMapping("/login")
    public Response login(@Valid @RequestBody LoginRequest dto) {
        return service.login(dto);
    }

    @PostMapping("/refresh")
    public Response refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("refreshToken is required");
        }
        return service.refreshToken(refreshToken);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public List<UserDto> getAllUsers() {
        return service.getAllUsers();
    }
}
