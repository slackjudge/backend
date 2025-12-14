package com.project.dto.response;

public record ProblemResponse(
        String title,
        int tierLevel,
        String link
) {
}
