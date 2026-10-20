package com.github.deepthi.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;


@Entity
@Table(name = "url_mapping")
public class UrlMapping 
{
	 @Id
	 @GeneratedValue(strategy = GenerationType.IDENTITY)
	 private Long id;
	 @Column(nullable = false, unique = true, length = 10)
	 private String shortCode;
	 @Column(nullable = false, length = 2048)
	 private String originalUrl;
	 @Column(nullable = false, updatable = false)
	 private LocalDateTime createdAt;
	 @Column(name = "expires_at")
	 private LocalDateTime expiresAt;
	 
	 @PrePersist
	 protected void onCreate() {
	     if (createdAt == null) {
	         createdAt = LocalDateTime.now();
	     }
	 }
	 
	 /*One detail to understand: LocalDateTime.now() uses the JVM's default timezone. 
	  * The value does not contain timezone information, 
	  * so we'll need to be consistent about timezone handling as the project grows.*/
	 
	 protected UrlMapping() {
		 
	 }
	 public UrlMapping(String shortCode, String originalUrl) 
	 {
		    this.shortCode = shortCode;
		    this.originalUrl = originalUrl;
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

	 public LocalDateTime getCreatedAt() {
		 return createdAt;
	 }

	 public Long getId() {
		 return id;
	 }
	 
	 public LocalDateTime getExpiresAt() {
		    return expiresAt;
		}

		public void setExpiresAt(LocalDateTime expiresAt) {
		    this.expiresAt = expiresAt;
		} 	
}
