package com.github.deepthi.dto;

import java.time.LocalDateTime;

public class CachedUrlMapping {

    private Long id;

    private String shortCode;

    private String originalUrl;

    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    public CachedUrlMapping() {
    }

    public CachedUrlMapping(
            Long id,
            String shortCode,
            String originalUrl,
            LocalDateTime createdAt,
            LocalDateTime expiresAt) {

        this.id = id;
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
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

    public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}