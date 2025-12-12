package com.project.dto.response;

public record LoginResponse(String accessToken, String refreshToken, boolean registeredUser
) {
}
