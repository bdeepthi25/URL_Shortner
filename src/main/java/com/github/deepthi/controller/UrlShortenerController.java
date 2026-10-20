package com.github.deepthi.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.github.deepthi.dto.CreateShortUrlRequest;
import com.github.deepthi.dto.CreateShortUrlResponse;
import com.github.deepthi.entity.UrlMapping;
import com.github.deepthi.service.UrlMappingService;

import jakarta.validation.Valid;

@RestController
public class UrlShortenerController {
	   private final UrlMappingService urlMappingService;
	   public UrlShortenerController(UrlMappingService urlMappingService)
	   {
		   this.urlMappingService = urlMappingService;
	   }
	   
	   @PostMapping("/api/urls")
	    public ResponseEntity<CreateShortUrlResponse> createShortUrl(@Valid @RequestBody CreateShortUrlRequest request)
	    {
		   UrlMapping mapping = urlMappingService.createShortUrl(request.getOriginalUrl(), request.getExpiresAt(),  request.getCustomAlias());
		   String shortUrl = ServletUriComponentsBuilder
	                .fromCurrentContextPath()
	                .path("/{shortCode}")
	                .buildAndExpand(mapping.getShortCode())
	                .toUriString();
		   CreateShortUrlResponse response =   new CreateShortUrlResponse(mapping.getShortCode(),
				   															shortUrl,
				   															mapping.getOriginalUrl(),
				   															mapping.getCreatedAt(),
				   															mapping.getExpiresAt());
		   return ResponseEntity.status(HttpStatus.CREATED).body(response);
	    }
	   @GetMapping("/{shortCode}")
	    public ResponseEntity<Void> redirectToOriginalUrl(
	            @PathVariable String shortCode) {

	        UrlMapping mapping =
	                urlMappingService.getOriginalUrl(shortCode);

	        return ResponseEntity
	                .status(HttpStatus.FOUND)
	                .location(URI.create(mapping.getOriginalUrl()))
	                .build();
	    }
	   

		@PutMapping("/api/urls/{shortCode}")
		public ResponseEntity<CreateShortUrlResponse> updateShortUrl(
		        @PathVariable String shortCode,
		        @Valid @RequestBody CreateShortUrlRequest request) {
		
		    UrlMapping mapping = urlMappingService.updateOriginalUrl(
		            shortCode,
		            request.getOriginalUrl());
		
		    String shortUrl = ServletUriComponentsBuilder
		            .fromCurrentContextPath()
		            .path("/{shortCode}")
		            .buildAndExpand(mapping.getShortCode())
		            .toUriString();
		
		    CreateShortUrlResponse response = new CreateShortUrlResponse(
		            mapping.getShortCode(),
		            shortUrl,
		            mapping.getOriginalUrl(),
		            mapping.getCreatedAt(),
		            mapping.getExpiresAt());
		
		    return ResponseEntity.ok(response);
		}
		

		@DeleteMapping("/api/urls/{shortCode}")
		public ResponseEntity<Void> deleteShortUrl(
		        @PathVariable String shortCode) {
		
		    urlMappingService.deleteShortUrl(shortCode);
		
		    return ResponseEntity.noContent().build();
		}


}
