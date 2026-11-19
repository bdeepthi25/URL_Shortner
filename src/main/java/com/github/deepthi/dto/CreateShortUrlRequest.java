package com.github.deepthi.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateShortUrlRequest {
	@NotBlank(message = "Original URL must not be empty.")
	@Size(max = 2048, message = "Original URL must not exceed 2048 characters.")
	private String originalUrl;
	private LocalDateTime expiresAt;
	private String customAlias;
    public String getCustomAlias() {
		return customAlias;
	}

	public void setCustomAlias(String customAlias) {
		this.customAlias = customAlias;
	}

	public CreateShortUrlRequest() {
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
