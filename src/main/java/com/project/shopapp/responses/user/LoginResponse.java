package com.project.shopapp.responses.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.shopapp.models.Role;
import lombok.*;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class LoginResponse {
    @JsonProperty("tokenType")
    private String tokenType;

    @JsonProperty("id")
    private Long id;

    @JsonProperty("username")
    private String username;

    @JsonProperty("roles")
    private Role roles;

    @JsonProperty("message")
    private String message;

    @JsonProperty("token")
    private String token;

    @JsonProperty("refresh_token")
    private String refreshToken;
}
