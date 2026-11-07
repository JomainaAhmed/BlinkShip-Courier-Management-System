package com.capg.auth.service;

import com.capg.auth.dto.LoginRequest;
import com.capg.auth.dto.Response;
import com.capg.auth.dto.SignupRequest;
import com.capg.auth.entity.User;
import com.capg.auth.exception.InvalidCredentialsException;
import com.capg.auth.exception.UserAlreadyExistsException;
import com.capg.auth.exception.UserNotFoundException;
import com.capg.auth.repository.AuthRepository;
import com.capg.auth.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthRepository repository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setEmail("test@blinkship.com");
        signupRequest.setPassword("Secret123");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("Secret123");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@blinkship.com");
        user.setPassword("encodedPassword");
        user.setRole("USER");
    }

    @Test
    void testSignup_Success() {
        when(repository.findByEmail("test@blinkship.com")).thenReturn(Optional.empty());
        when(repository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Secret123")).thenReturn("encodedPassword");

        assertDoesNotThrow(() -> authService.signup(signupRequest));
        verify(repository, times(1)).save(any(User.class));
    }

    @Test
    void testSignup_EmailAlreadyExists() {
        when(repository.findByEmail("test@blinkship.com")).thenReturn(Optional.of(user));

        assertThrows(UserAlreadyExistsException.class, () -> authService.signup(signupRequest));
        verify(repository, never()).save(any(User.class));
    }

    @Test
    void testSignup_UsernameAlreadyExists() {
        when(repository.findByEmail("test@blinkship.com")).thenReturn(Optional.empty());
        when(repository.findByUsername("testuser")).thenReturn(Optional.of(user));

        assertThrows(UserAlreadyExistsException.class, () -> authService.signup(signupRequest));
        verify(repository, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        when(repository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secret123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("test@blinkship.com", "testuser", "USER")).thenReturn("mock-access-token");
        when(jwtUtil.generateRefreshToken("test@blinkship.com", "testuser", "USER")).thenReturn("mock-refresh-token");

        Response response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mock-access-token", response.getToken());
        assertEquals("mock-refresh-token", response.getRefreshToken());
    }

    @Test
    void testLogin_UserNotFound() {
        when(repository.findByUsername("unknown")).thenReturn(Optional.empty());
        loginRequest.setUsername("unknown");

        assertThrows(UserNotFoundException.class, () -> authService.login(loginRequest));
    }

    @Test
    void testLogin_InvalidPassword() {
        when(repository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "encodedPassword")).thenReturn(false);
        loginRequest.setPassword("WrongPassword");

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
    }
}
