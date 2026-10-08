package com.company.finance.financesystem.dto.auth;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds
) {
    public static AuthResponse of(String access, String refresh, long accessTtl) {
        return new AuthResponse(access, refresh, "Bearer", accessTtl);
    }
}