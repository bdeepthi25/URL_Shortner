package com.github.deepthi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.deepthi.entity.UrlMapping;
import com.github.deepthi.exception.ShortUrlNotFoundException;
import com.github.deepthi.repository.UrlMappingRepository;

@ExtendWith(MockitoExtension.class)
public class UrlMappingServiceTest {
	
	   @Mock
	    private UrlMappingRepository urlMappingRepository;

	    private UrlMappingService urlMappingService;

	    @BeforeEach
	    void setUp() {
	        urlMappingService =
	                new UrlMappingService(urlMappingRepository);
	    }

	    @Test
	    void createShortUrlShouldSaveMappingForValidUrl() {

	        String originalUrl = "https://example.com/products";

	        when(urlMappingRepository.existsByShortCode(anyString()))
	                .thenReturn(false);

	        when(urlMappingRepository.save(any(UrlMapping.class)))
	                .thenAnswer(invocation -> invocation.getArgument(0));

	        UrlMapping result =
	                urlMappingService.createShortUrl(originalUrl);

	        assertNotNull(result);
	        assertEquals(originalUrl, result.getOriginalUrl());
	        assertNotNull(result.getShortCode());
	        assertEquals(8, result.getShortCode().length());

	        verify(urlMappingRepository).save(any(UrlMapping.class));
	    }

	    @Test
	    void createShortUrlShouldRejectBlankUrl() {

	        assertThrows(
	                IllegalArgumentException.class,
	                () -> urlMappingService.createShortUrl("  ")
	        );

	        verifyNoInteractions(urlMappingRepository);
	    }

	    @Test
	    void createShortUrlShouldRejectMalformedUrl() {

	        assertThrows(
	                IllegalArgumentException.class,
	                () -> urlMappingService.createShortUrl(
	                        "https://exa mple.com")
	        );

	        verifyNoInteractions(urlMappingRepository);
	    }

	    @Test
	    void createShortUrlShouldRetryWhenCodeAlreadyExists() {

	        String originalUrl = "https://example.com";

	        when(urlMappingRepository.existsByShortCode(anyString()))
	                .thenReturn(true, false);

	        when(urlMappingRepository.save(any(UrlMapping.class)))
	                .thenAnswer(invocation -> invocation.getArgument(0));

	        UrlMapping result =
	                urlMappingService.createShortUrl(originalUrl);

	        assertNotNull(result);
	        assertEquals(originalUrl, result.getOriginalUrl());

	        verify(urlMappingRepository, times(2))
	                .existsByShortCode(anyString());

	        verify(urlMappingRepository).save(any(UrlMapping.class));
	    }

	    @Test
	    void createShortUrlShouldFailAfterMaximumCollisionAttempts() {

	        when(urlMappingRepository.existsByShortCode(anyString()))
	                .thenReturn(true);

	        IllegalStateException exception = assertThrows(
	                IllegalStateException.class,
	                () -> urlMappingService.createShortUrl(
	                        "https://example.com")
	        );

	        assertTrue(exception.getMessage()
	                .contains("Unable to generate a unique short code"));

	        verify(urlMappingRepository, times(5))
	                .existsByShortCode(anyString());

	        verify(urlMappingRepository, times(0))
	                .save(any(UrlMapping.class));
	    }
	    

		@Test
		void getOriginalUrlShouldReturnMappingWhenShortCodeExists() {
		
		    String shortCode = "aB7xK2pQ";
		    String originalUrl = "https://example.com/products";
		
		    UrlMapping mapping = new UrlMapping(shortCode, originalUrl);
		
		    when(urlMappingRepository.findByShortCode(shortCode))
		            .thenReturn(java.util.Optional.of(mapping));
		
		    UrlMapping result =
		            urlMappingService.getOriginalUrl(shortCode);
		
		    assertNotNull(result);
		    assertEquals(shortCode, result.getShortCode());
		    assertEquals(originalUrl, result.getOriginalUrl());
		
		    verify(urlMappingRepository).findByShortCode(shortCode);
		}
		
		@Test
		void getOriginalUrlShouldThrowExceptionWhenShortCodeDoesNotExist() {
		
		    String shortCode = "unknown1";
		
		    when(urlMappingRepository.findByShortCode(shortCode))
		            .thenReturn(java.util.Optional.empty());
		
		    assertThrows(
		    		   ShortUrlNotFoundException.class,
		            () -> urlMappingService.getOriginalUrl(shortCode)
		    );
		
		    verify(urlMappingRepository).findByShortCode(shortCode);
		}

	@Test
	void createShortUrlShouldRejectUrlWithoutHttpScheme() {
	    assertThrows(IllegalArgumentException.class,
	            () -> urlMappingService.createShortUrl(
	                    "ftp://example.com/file"));
	}

	@Test
	void createShortUrlShouldRejectUrlWithoutHost() {
	    assertThrows(IllegalArgumentException.class,
	            () -> urlMappingService.createShortUrl(
	                    "https:///products"));
	}
	
	@Test
	void createShortUrlShouldRejectUrlWithoutScheme() {
	    assertThrows(IllegalArgumentException.class,
	            () -> urlMappingService.createShortUrl(
	                    "www.example.com"));
	}


}
