package com.github.deepthi.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncoderTest {

	   @Test
	    void shouldEncodeAndMatchPassword() {

	        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	        String rawPassword = "learningPassword123";

	        // Encode the same password twice
	        String encodedPassword1 = passwordEncoder.encode(rawPassword);
	        String encodedPassword2 = passwordEncoder.encode(rawPassword);

	        // BCrypt generates different hashes because of random salts
	        assertNotEquals(encodedPassword1, encodedPassword2);

	        // Correct password should match
	        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword1));

	        // Incorrect password should not match
	        assertFalse(passwordEncoder.matches("wrongPassword", encodedPassword1));
	   }
}
