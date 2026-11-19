package com.github.deepthi.exception;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(ShortUrlNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleShortUrlNotFound( ShortUrlNotFoundException exception)
	{
		  Map<String, String> errorResponse = Map.of(
	                "error", "Short URL Not Found",
	                "message", exception.getMessage()
	        );
		  return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}
	 @ExceptionHandler(IllegalArgumentException.class)
	    public ResponseEntity<Map<String, String>> handleIllegalArgument(
	            IllegalArgumentException exception) {

	        Map<String, String> errorResponse = Map.of(
	                "error", "Bad Request",
	                "message", exception.getMessage()
	        );

	        return ResponseEntity
	                .status(HttpStatus.BAD_REQUEST)
	                .body(errorResponse);
	    }
	 @ExceptionHandler(MethodArgumentNotValidException.class)
	 public ResponseEntity<Map<String, String>> handleValidationException(
	         MethodArgumentNotValidException exception) {

	     String message = exception.getBindingResult()
	             .getFieldErrors()
	             .stream()
	             .map(FieldError::getDefaultMessage)
	             .filter(error -> error != null)
	             .distinct()
	             .collect(Collectors.joining("; "));

	     if (message.isBlank()) {
	         message = "Invalid request.";
	     }

	     Map<String, String> errorResponse = Map.of(
	             "error", "Bad Request",
	             "message", message
	     );

	     return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	             .body(errorResponse);
	 }
	 @ExceptionHandler(DataIntegrityViolationException.class)
	 public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(
	         DataIntegrityViolationException exception) {

	     Map<String, String> errorResponse = Map.of(
	             "error", "Conflict",
	             "message", "The requested resource conflicts with existing data."
	     );

	     return ResponseEntity
	             .status(HttpStatus.CONFLICT)
	             .body(errorResponse);
	 }
}
