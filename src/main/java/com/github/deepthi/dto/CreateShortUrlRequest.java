package com.github.deepthi.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateShortUrlRequest {
	@NotBlank(message = "Original URL must not be empty.")
	private String originalUrl;

    public CreateShortUrlRequest() {
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }
}
