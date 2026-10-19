package com.github.deepthi.service;

import java.net.URI;
import java.security.SecureRandom;

import org.springframework.stereotype.Service;

import com.github.deepthi.entity.UrlMapping;
//import java.net.URI;
import com.github.deepthi.repository.UrlMappingRepository;
import com.github.deepthi.exception.ShortUrlNotFoundException;

@Service
public class UrlMappingService {
	private final UrlMappingRepository urlMappingRepository;
	
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
    
    public UrlMapping createShortUrl(String originalUrl) {
    	 validateUrl(originalUrl);
    	 for(int i =0; i<MAX_ATTEMPTS ; i++)
    	 {
    		 String shortCode = generateShortCode();
    		 if(! urlMappingRepository.existsByShortCode(shortCode))
    		 {
    			 UrlMapping urlMapping = new UrlMapping(shortCode, originalUrl);
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
		
		    return urlMappingRepository.findByShortCode(shortCode)
		            .orElseThrow(() -> new ShortUrlNotFoundException(
		                    "Short URL not found for code: " + shortCode));
		}

}
