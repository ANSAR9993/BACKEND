package com.dgapr.demo.Dto.AuthDto;

import com.dgapr.demo.Model.Role;
import lombok.*;

@Getter
@Builder
public class AuthResponse {
    private final boolean success;
    private final String message;
    private final String token;
    private final String role;
}
