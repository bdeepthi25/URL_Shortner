package com.github.deepthi.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class GlobalExceptionHandlerTest {
	 private GlobalExceptionHandler exceptionHandler;

	    @BeforeEach
	    void setUp() {
	        exceptionHandler = new GlobalExceptionHandler();
	    }

	    @Test
	    void handleShortUrlNotFoundShouldReturn404() {

	        ShortUrlNotFoundException exception =
	                new ShortUrlNotFoundException(
	                        "Short URL not found for code: unknown1");

	        ResponseEntity<Map<String, String>> response =
	                exceptionHandler.handleShortUrlNotFound(exception);

	        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

	        assertNotNull(response.getBody());

	        assertEquals(
	                "Short URL Not Found",
	                response.getBody().get("error"));

	        assertEquals(
	                "Short URL not found for code: unknown1",
	                response.getBody().get("message"));
	    }

	    @Test
	    void handleIllegalArgumentShouldReturn400() {

	        IllegalArgumentException exception =
	                new IllegalArgumentException(
	                        "Please provide a valid HTTP or HTTPS URL.");

	        ResponseEntity<Map<String, String>> response =
	                exceptionHandler.handleIllegalArgument(exception);

	        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

	        assertNotNull(response.getBody());

	        assertEquals(
	                "Bad Request",
	                response.getBody().get("error"));

	        assertEquals(
	                "Please provide a valid HTTP or HTTPS URL.",
	                response.getBody().get("message"));
	    }
}
