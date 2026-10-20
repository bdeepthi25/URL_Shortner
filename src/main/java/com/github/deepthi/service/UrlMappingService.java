package com.github.deepthi.service;

import java.net.URI;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.github.deepthi.entity.UrlMapping;
//import java.net.URI;
import com.github.deepthi.repository.UrlMappingRepository;
import com.github.deepthi.exception.ShortUrlNotFoundException;

@Service
public class UrlMappingService {
	private final UrlMappingRepository urlMappingRepository;
	
	@Value("${url.cleanup.retention-days}")
	private int retentionDays;
	
	public UrlMappingService(UrlMappingRepository urlMappingRepository)
	{
		this.urlMappingRepository = urlMappingRepository;
	}
	

    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            + "abcdefghijklmnopqrstuvwxyz"
            + "0123456789";

    private static final int SHORT_CODE_LENGTH = 8;
    private static final int MAX_ATTEMPTS = 5;
    private final SecureRandom random = new SecureRandom();
    
    private static final int MAX_ALIAS_LENGTH = 10;

    private static final Set<String> RESERVED_ALIASES = Set.of(
            "api",
            "swagger-ui",
            "v3",
            "actuator",
            "error"
    );
    
//    public UrlMapping createShortUrl(String originalUrl, LocalDateTime expiresAt) {
//    	 validateUrl(originalUrl);
//    	 if (expiresAt != null && !expiresAt.isAfter(LocalDateTime.now())) {
//			    throw new IllegalArgumentException(
//			            "Expiration date must be in the future.");
//    	     }
//    	 for(int i =0; i<MAX_ATTEMPTS ; i++)
//    	 {
//    		 String shortCode = generateShortCode();
//    		 if(! urlMappingRepository.existsByShortCode(shortCode))
//    		 {
//    			 UrlMapping urlMapping = new UrlMapping(shortCode, originalUrl);
//    			 urlMapping.setExpiresAt(expiresAt);
//    			 return urlMappingRepository.save(urlMapping);
//    		 }
//    		
//    		
//    	 }
//    	   throw new IllegalStateException(
//                   "Unable to generate a unique short code. Please try again.");	
//    }
    

	public UrlMapping createShortUrl(
	        String originalUrl,
	        LocalDateTime expiresAt) {
	
	    return createShortUrl(originalUrl, expiresAt, null);
	}

	public UrlMapping createShortUrl(
	        String originalUrl,
	        LocalDateTime expiresAt,
	        String customAlias) {
	
	    validateUrl(originalUrl);
	    validateCustomAlias(customAlias);
	
	    if (expiresAt != null
	            && !expiresAt.isAfter(LocalDateTime.now())) {
	        throw new IllegalArgumentException(
	                "Expiration date must be in the future.");
	    }
	
	    // Use the requested alias when one is provided.
	    if (customAlias != null && !customAlias.isBlank()) {
	
	        if (urlMappingRepository.existsByShortCode(customAlias)) {
	            throw new IllegalArgumentException(
	                    "Custom alias is already in use.");
	        }
	
	        UrlMapping urlMapping =
	                new UrlMapping(customAlias, originalUrl);
	
	        urlMapping.setExpiresAt(expiresAt);
	
	        return urlMappingRepository.save(urlMapping);
	    }
	
	    // Otherwise, preserve the existing random-code generation behavior.
	    for (int i = 0; i < MAX_ATTEMPTS; i++) {
	
	        String shortCode = generateShortCode();
	
	        if (!urlMappingRepository.existsByShortCode(shortCode)) {
	
	            UrlMapping urlMapping =
	                    new UrlMapping(shortCode, originalUrl);
	
	            urlMapping.setExpiresAt(expiresAt);
	
	            return urlMappingRepository.save(urlMapping);
	        }
	    }
	
	    throw new IllegalStateException(
	            "Unable to generate a unique short code. Please try again.");
	}

	private String generateShortCode() {
		  StringBuilder shortCode = new StringBuilder();
		  for(int i=0;i<SHORT_CODE_LENGTH ; i++)
		  {
			  int index = random.nextInt(CHARACTERS.length());
			  shortCode.append(CHARACTERS.charAt(index));
		  }
		return shortCode.toString();
	}
	private void validateUrl(String originalUrl) 
	{
		if(originalUrl.isBlank() || originalUrl == null)
		{
			throw new IllegalArgumentException("Original URL must not be empty.");
		}
		URI uri;
		try{
			 uri = URI.create(originalUrl);
		}catch (IllegalArgumentException exception) {
	        throw new IllegalArgumentException(
	                "Please provide a valid HTTP or HTTPS URL.",
	                exception);
	    }
		
		String scheme = uri.getScheme();
		if(scheme == null || 
				!(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")) ||
				uri.getHost() == null)
		{
			throw new IllegalArgumentException("Please provide a valid HTTP or HTTPS URL.");
		}
	}

		public UrlMapping getOriginalUrl(String shortCode) {
		
		    if (shortCode == null || shortCode.isBlank()) {
		        throw new IllegalArgumentException(
		                "Short code must not be empty.");
		    }
		
		    UrlMapping urlMapping =  urlMappingRepository.findByShortCode(shortCode)
		            .orElseThrow(() -> new ShortUrlNotFoundException(
		                    "Short URL not found for code: " + shortCode));
		    
		    if (urlMapping.getExpiresAt() != null
		            && !urlMapping.getExpiresAt().isAfter(LocalDateTime.now())) {
		        throw new ShortUrlNotFoundException(shortCode);
		    }
		    
		    return urlMapping;
		}

		public UrlMapping updateOriginalUrl(String shortCode, String newOriginalUrl) 
		{
			 if (shortCode == null || shortCode.isBlank()) 
			 {
			        throw new IllegalArgumentException(
			                "Short code must not be empty.");
			    }
			    validateUrl(newOriginalUrl);

			    UrlMapping urlMapping = getOriginalUrl(shortCode);

			    urlMapping.setOriginalUrl(newOriginalUrl);

			    return urlMappingRepository.save(urlMapping);
		}

		public void deleteShortUrl(String shortCode) {
		    if (shortCode == null || shortCode.isBlank()) {
		        throw new IllegalArgumentException("Short code must not be empty.");
		    }
		
		    UrlMapping urlMapping = urlMappingRepository.findByShortCode(shortCode)
		            .orElseThrow(() -> new ShortUrlNotFoundException(shortCode));
		
		    urlMappingRepository.delete(urlMapping);
		}
		public int cleanupExpiredUrls() {
		    LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);

		    return urlMappingRepository.deleteExpiredUrlsBefore(cutoff);
		}
		
		private void validateCustomAlias(String customAlias) {

		    if (customAlias == null || customAlias.isBlank()) {
		        return;
		    }

		    if (customAlias.length() > MAX_ALIAS_LENGTH) {
		        throw new IllegalArgumentException(
		                "Custom alias must not exceed 10 characters.");
		    }

		    if (!customAlias.matches("[a-zA-Z0-9]+")) {
		        throw new IllegalArgumentException(
		                "Custom alias can contain only letters and numbers.");
		    }

		    if (RESERVED_ALIASES.contains(customAlias.toLowerCase(Locale.ROOT))) {
		        throw new IllegalArgumentException(
		                "This custom alias is reserved. Please choose another alias.");
		    }
		}
		
		public Page<UrlMapping> getAllUrls(Pageable pageable) {
		    return urlMappingRepository.findAll(pageable);
		}
}
