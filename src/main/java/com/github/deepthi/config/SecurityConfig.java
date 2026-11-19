package com.github.deepthi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	   @Value("${ADMIN_USERNAME}")
	    private String adminUsername;

	    @Value("${ADMIN_PASSWORD}")
	    private String adminPassword;

	    @Bean
	    public PasswordEncoder passwordEncoder() {
	        return new BCryptPasswordEncoder();
	    }

	    @Bean
	    public UserDetailsService userDetailsService(
	            PasswordEncoder passwordEncoder) {

	        UserDetails admin = User.builder()
	                .username(adminUsername)
	                .password(passwordEncoder.encode(adminPassword))
	                .roles("ADMIN")
	                .build();

	        return new InMemoryUserDetailsManager(admin);
	    }

	    @Bean
	    public SecurityFilterChain securityFilterChain(
	            HttpSecurity http) throws Exception {

	        http
	            .csrf(csrf -> csrf.disable())
	            .sessionManagement(session -> session
	                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	            .authorizeHttpRequests(auth -> auth
	            		.requestMatchers("/actuator/health/db").hasRole("ADMIN")
	            		.requestMatchers("/actuator/health/redis").hasRole("ADMIN")
	            		.requestMatchers("/actuator/health/**").permitAll()
	            		 .requestMatchers("/actuator/info").hasRole("ADMIN")
	            		 .requestMatchers("/actuator/metrics/**").hasRole("ADMIN")
	                .requestMatchers(HttpMethod.POST, "/api/urls").permitAll()
	                .requestMatchers(HttpMethod.GET, "/{shortCode}").permitAll()
	                .requestMatchers("/api/urls/**").hasRole("ADMIN")
	                .anyRequest().denyAll()
	            )
	            .httpBasic(basic -> {});

	        return http.build();
	    }
}
