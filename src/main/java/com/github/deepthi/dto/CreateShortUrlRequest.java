package com.github.deepthi.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;

public class CreateShortUrlRequest {
	@NotBlank(message = "Original URL must not be empty.")
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
