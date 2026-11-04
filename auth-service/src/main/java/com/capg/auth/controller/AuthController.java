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

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public List<UserDto> getAllUsers() {
        return service.getAllUsers();
    }
}
