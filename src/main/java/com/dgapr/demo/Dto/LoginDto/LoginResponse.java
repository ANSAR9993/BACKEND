package com.dgapr.demo.Dto.LoginDto;

public record LoginResponse(
        String token,
        String tokenType
) {}
