package com.github.deepthi.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;



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


@Test
void handleValidationExceptionShouldReturnDefaultMessageWhenNoFieldErrorExists()
        throws Exception {
	Map<String, Object> target = new HashMap<>();

	MapBindingResult bindingResult =
	        new MapBindingResult(target, "request");
    Method method = GlobalExceptionHandlerTest.class
            .getDeclaredMethod("sampleMethod", String.class);

    MethodParameter parameter = new MethodParameter(method, 0);

    MethodArgumentNotValidException exception =
            new MethodArgumentNotValidException(parameter, bindingResult);

    ResponseEntity<Map<String, String>> response =
            exceptionHandler.handleValidationException(exception);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Bad Request", response.getBody().get("error"));
    assertEquals("Invalid request.", response.getBody().get("message"));
}

@Test
void handleValidationExceptionShouldReturnFieldErrorMessage()
        throws Exception {

	Map<String, Object> target = new HashMap<>();

	MapBindingResult bindingResult =
	        new MapBindingResult(target, "request");

    bindingResult.rejectValue(
            "name", "NotBlank", "Name must not be blank.");

    Method method = GlobalExceptionHandlerTest.class
            .getDeclaredMethod("sampleMethod", String.class);

    MethodParameter parameter = new MethodParameter(method, 0);

    MethodArgumentNotValidException exception =
            new MethodArgumentNotValidException(parameter, bindingResult);

    ResponseEntity<Map<String, String>> response =
            exceptionHandler.handleValidationException(exception);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Name must not be blank.",
            response.getBody().get("message"));
}

@SuppressWarnings("unused")
private void sampleMethod(String value) {
}


@Test
void handleValidationExceptionShouldReturnFallbackWhenFieldErrorMessageIsNull()
        throws Exception {

    Map<String, Object> target = new HashMap<>();

    MapBindingResult bindingResult =
            new MapBindingResult(target, "request");

    bindingResult.rejectValue("name", "NotBlank", (String) null);

    Method method = GlobalExceptionHandlerTest.class
            .getDeclaredMethod("sampleMethod", String.class);

    MethodParameter parameter = new MethodParameter(method, 0);

    MethodArgumentNotValidException exception =
            new MethodArgumentNotValidException(parameter, bindingResult);

    ResponseEntity<Map<String, String>> response =
            exceptionHandler.handleValidationException(exception);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Bad Request", response.getBody().get("error"));
    assertEquals("Invalid request.", response.getBody().get("message"));
}

@Test
void handleDataIntegrityViolationShouldReturn409() {

    DataIntegrityViolationException exception =
            new DataIntegrityViolationException("Duplicate short code");

    ResponseEntity<Map<String, String>> response =
            exceptionHandler.handleDataIntegrityViolation(exception);

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

    assertNotNull(response.getBody());

    assertEquals("Conflict", response.getBody().get("error"));

    assertEquals(
            "The requested resource conflicts with existing data.",
            response.getBody().get("message")
    );
}

}
