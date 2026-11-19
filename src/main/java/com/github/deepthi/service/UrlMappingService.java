package com.github.deepthi.service;

import java.net.URI;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.deepthi.dto.CachedUrlMapping;
import com.github.deepthi.entity.UrlMapping;
import com.github.deepthi.repository.UrlClickRepository;
//import java.net.URI;
import com.github.deepthi.repository.UrlMappingRepository;

import com.github.deepthi.exception.ShortUrlNotFoundException;

@Service
public class UrlMappingService {

	private static final Logger logger =
	        LoggerFactory.getLogger(UrlMappingService.class);
	private final UrlMappingRepository urlMappingRepository;
	private final UrlClickRepository urlClickRepository;
	private final RedisTemplate<String, CachedUrlMapping> urlMappingRedisTemplate;

	@Value("${url.cleanup.retention-days}")
	private int retentionDays;
	@Value("${url.cache.ttl-seconds}")
	private long cacheTtlSeconds;

	public UrlMappingService(
	        UrlMappingRepository urlMappingRepository,
	        UrlClickRepository urlClickRepository,
	        RedisTemplate<String, CachedUrlMapping> urlMappingRedisTemplate) {

	    this.urlMappingRepository = urlMappingRepository;
	    this.urlClickRepository = urlClickRepository;
	    this.urlMappingRedisTemplate = urlMappingRedisTemplate;
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

//	        return urlMappingRepository.save(urlMapping);

	        UrlMapping savedMapping = urlMappingRepository.save(urlMapping);

	        logger.info("Custom short URL created successfully. shortCode={}",
	                savedMapping.getShortCode());

	        return savedMapping;
	    }

	    // Otherwise, preserve the existing random-code generation behavior.
	    for (int i = 0; i < MAX_ATTEMPTS; i++) {

	        String shortCode = generateShortCode();

	        if (!urlMappingRepository.existsByShortCode(shortCode)) {

	            UrlMapping urlMapping =
	                    new UrlMapping(shortCode, originalUrl);

	            urlMapping.setExpiresAt(expiresAt);

//	            return urlMappingRepository.save(urlMapping);
	            UrlMapping savedMapping = urlMappingRepository.save(urlMapping);

	            logger.info("Short URL created successfully. shortCode={}",
	                    savedMapping.getShortCode());

	            return savedMapping;
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
		if (originalUrl == null || originalUrl.isBlank())
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

	private void validateShortCode(String shortCode) {

	    if (shortCode == null || shortCode.isBlank()) {
	        throw new IllegalArgumentException(
	                "Short code must not be empty.");
	    }

	    if (!shortCode.matches("[a-zA-Z0-9]{1,10}")) {
	        throw new IllegalArgumentException(
	                "Short code must contain only letters and numbers "
	                + "and must not exceed 10 characters.");
	    }
	}

//		public UrlMapping getOriginalUrl(String shortCode) {
//
//		    if (shortCode == null || shortCode.isBlank()) {
//		        throw new IllegalArgumentException(
//		                "Short code must not be empty.");
//		    }
//
//		    UrlMapping urlMapping =  urlMappingRepository.findByShortCode(shortCode)
//		            .orElseThrow(() -> new ShortUrlNotFoundException(
//		                    "Short URL not found for code: " + shortCode));
//
//		    if (urlMapping.getExpiresAt() != null
//		            && !urlMapping.getExpiresAt().isAfter(LocalDateTime.now())) {
//		        throw new ShortUrlNotFoundException(shortCode);
//		    }
//
//		    return urlMapping;
//		}


		public UrlMapping getOriginalUrl(String shortCode) {

//		    if (shortCode == null || shortCode.isBlank()) {
//		        throw new IllegalArgumentException(
//		                "Short code must not be empty.");
//		    }

			validateShortCode(shortCode);

		    String cacheKey = "url:" + shortCode;

		    // Step 1: Check Redis first.
		    CachedUrlMapping cached =
		            urlMappingRedisTemplate.opsForValue().get(cacheKey);

			  if (cached != null)
			  {
				  logger.debug("Redis cache hit. shortCode={}", shortCode);
			    // Check whether the cached URL has expired.
			    if (cached.getExpiresAt() != null
			            && !cached.getExpiresAt().isAfter(LocalDateTime.now())) {
			     logger.debug("Expired cached URL removed. shortCode={}", shortCode);
			        urlMappingRedisTemplate.delete(cacheKey);
			        throw new ShortUrlNotFoundException(shortCode);
			    }
			    // Reconstruct UrlMapping directly from Redis.
			    UrlMapping urlMapping = new UrlMapping(
			            cached.getId(),
			            cached.getShortCode(),
			            cached.getOriginalUrl(),
			            cached.getCreatedAt(),
			            cached.getExpiresAt());

			    return urlMapping;
			}
			  logger.debug("Redis cache miss. Fetching URL from database. shortCode={}",
				        shortCode);
		    // Step 2: Cache miss — fetch from PostgreSQL.
		    UrlMapping urlMapping = urlMappingRepository.findByShortCode(shortCode)
		            .orElseThrow(() -> new ShortUrlNotFoundException(
		                    "Short URL not found for code: " + shortCode));

		    // Step 3: Check expiration before caching.
		    if (urlMapping.getExpiresAt() != null
		            && !urlMapping.getExpiresAt().isAfter(LocalDateTime.now())) {

		        throw new ShortUrlNotFoundException(shortCode);
		    }

		    // Step 4: Cache the successful lookup.
		    CachedUrlMapping cacheValue = new CachedUrlMapping(
		            urlMapping.getId(),
		            urlMapping.getShortCode(),
		            urlMapping.getOriginalUrl(),
		            urlMapping.getCreatedAt(),
		            urlMapping.getExpiresAt());

//		    urlMappingRedisTemplate.opsForValue().set(cacheKey, cacheValue);

		    urlMappingRedisTemplate.opsForValue()
	        .set(cacheKey, cacheValue,
	                java.time.Duration.ofSeconds(cacheTtlSeconds));

		    return urlMapping;
		}




		public UrlMapping updateOriginalUrl(String shortCode, String newOriginalUrl)
		{
//			 if (shortCode == null || shortCode.isBlank())
//			 {
//			        throw new IllegalArgumentException(
//			                "Short code must not be empty.");
//			    }

			validateShortCode(shortCode);
			    validateUrl(newOriginalUrl);

			    UrlMapping urlMapping = getOriginalUrl(shortCode);

			    urlMapping.setOriginalUrl(newOriginalUrl);

			    UrlMapping updatedMapping = urlMappingRepository.save(urlMapping);

			    // Invalidate the cached destination so the next lookup
			    // fetches the updated mapping from PostgreSQL.
			    urlMappingRedisTemplate.delete("url:" + shortCode);

			    logger.info("Short URL destination updated successfully. shortCode={}",
			            shortCode);
			   // return urlMappingRepository.save(urlMapping);
			    return updatedMapping;
		}

		@Transactional
		public void deleteShortUrl(String shortCode) {
//		    if (shortCode == null || shortCode.isBlank()) {
//		        throw new IllegalArgumentException("Short code must not be empty.");
//		    }
//

			validateShortCode(shortCode);
		    UrlMapping urlMapping = urlMappingRepository.findByShortCode(shortCode)
		            .orElseThrow(() -> new ShortUrlNotFoundException(shortCode));

		    urlClickRepository.deleteByUrlMapping_ShortCode(shortCode);
		    urlMappingRepository.delete(urlMapping);

		    // Invalidate the corresponding Redis cache entry.
		    urlMappingRedisTemplate.delete("url:" + shortCode);
		    logger.info("Short URL deleted successfully. shortCode={}",
		            shortCode);
		}
		@Transactional
		public int cleanupExpiredUrls() {
		    LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);

		    urlClickRepository.deleteClicksForExpiredUrlsBefore(cutoff);

		    int deletedCount = urlMappingRepository.deleteExpiredUrlsBefore(cutoff);

		    logger.info("Expired URL cleanup completed. deletedCount={}, retentionDays={}",
		            deletedCount, retentionDays);

		    return deletedCount;
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
