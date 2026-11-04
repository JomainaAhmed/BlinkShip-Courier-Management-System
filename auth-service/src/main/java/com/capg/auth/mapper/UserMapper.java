package com.capg.auth.mapper;

import com.capg.auth.dto.SignupRequest;
import com.capg.auth.dto.Response;
import com.capg.auth.dto.UserDto;
import com.capg.auth.entity.User;

public class UserMapper {

    public static User toEntity(SignupRequest dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setRole("USER");
        return user;
    }
    
    public static UserDto toDto(User user) {
        if (user == null) return null;
        return new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }
    
    public static Response toResponse(String token) {
        return new Response(token);
    }
}
