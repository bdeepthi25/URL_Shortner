package com.github.deepthi.dto;

import java.time.LocalDateTime;

public record CreateShortUrlResponse(String shortCode,
        String shortUrl,
        String originalUrl,
        LocalDateTime createdAt) {

}
