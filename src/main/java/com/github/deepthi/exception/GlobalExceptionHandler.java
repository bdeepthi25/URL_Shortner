package com.github.deepthi.exception;

import java.util.Map;

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
	 public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException exception)
	 {

		    String message = "Invalid request.";

		    FieldError fieldError = exception.getBindingResult().getFieldError();
		    if (fieldError != null && fieldError.getDefaultMessage() != null) {
		        message = fieldError.getDefaultMessage();
		    }

		    Map<String, String> errorResponse = Map.of(
		            "error", "Bad Request",
		            "message", message);

		    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
		            .body(errorResponse);
	 }
}
