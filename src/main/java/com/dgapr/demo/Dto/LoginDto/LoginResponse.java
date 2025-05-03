package com.dgapr.demo.Dto.LoginDto;

public record LoginResponse(
        String accessToken,
        String tokenType // e.g. "Bearer"
) {}
