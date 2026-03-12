package com.creditcard.dto;

import lombok.Data;
import java.util.Set;

@Data
public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private String username;
    private String fullName;
    private Set<String> roles;

    public AuthResponse(String token, String username, String fullName, Set<String> roles) {
        this.token = token;
        this.username = username;
        this.fullName = fullName;
        this.roles = roles;
    }
}
