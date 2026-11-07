package com.capg.auth.dto;

public class Response {

    private String token;
    private String refreshToken;

    public Response() {
    }

    public Response(String token) {
        this.token = token;
    }

    public Response(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
