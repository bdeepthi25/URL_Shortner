package com.github.deepthi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
	        ReflectionTestUtils.setField(
		            urlMappingService,
		            "retentionDays",
		            30
		    );
	    }

	    @Test
	    void createShortUrlShouldSaveMappingForValidUrl() {

	        String originalUrl = "https://example.com/products";

	        when(urlMappingRepository.existsByShortCode(anyString()))
	                .thenReturn(false);

	        when(urlMappingRepository.save(any(UrlMapping.class)))
	                .thenAnswer(invocation -> invocation.getArgument(0));

	        UrlMapping result =
	                urlMappingService.createShortUrl(originalUrl, null);

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
	                () -> urlMappingService.createShortUrl("  ", null)
	        );

	        verifyNoInteractions(urlMappingRepository);
	    }

	    @Test
	    void createShortUrlShouldRejectMalformedUrl() {

	        assertThrows(
	                IllegalArgumentException.class,
	                () -> urlMappingService.createShortUrl(
	                        "https://exa mple.com", null)
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
	                urlMappingService.createShortUrl(originalUrl, null);

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
	                        "https://example.com", null)
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
	                    "ftp://example.com/file", null));
	}

	@Test
	void createShortUrlShouldRejectUrlWithoutHost() {
	    assertThrows(IllegalArgumentException.class,
	            () -> urlMappingService.createShortUrl(
	                    "https:///products", null));
	}
	
	@Test
	void createShortUrlShouldRejectUrlWithoutScheme() {
	    assertThrows(IllegalArgumentException.class,
	            () -> urlMappingService.createShortUrl(
	                    "www.example.com", null));
	}


	@Test
	void updateOriginalUrlShouldUpdateExistingMapping() {
	
	    String shortCode = "aB7xK2pQ";
	    String oldUrl = "https://example.com/old-page";
	    String newUrl = "https://example.com/new-page";
	
	    UrlMapping existingMapping =
	            new UrlMapping(shortCode, oldUrl);
	
	    when(urlMappingRepository.findByShortCode(shortCode))
	            .thenReturn(java.util.Optional.of(existingMapping));
	
	    when(urlMappingRepository.save(any(UrlMapping.class)))
	            .thenAnswer(invocation -> invocation.getArgument(0));
	
	    UrlMapping result =
	            urlMappingService.updateOriginalUrl(shortCode, newUrl);
	
	    assertEquals(shortCode, result.getShortCode());
	    assertEquals(newUrl, result.getOriginalUrl());
	
	    verify(urlMappingRepository).findByShortCode(shortCode);
	    verify(urlMappingRepository).save(existingMapping);
	}

	
	@Test
	void updateOriginalUrlShouldThrowExceptionWhenShortCodeDoesNotExist() {
	
	    String shortCode = "unknown1";
	    String newUrl = "https://example.com/new-page";
	
	    when(urlMappingRepository.findByShortCode(shortCode))
	            .thenReturn(java.util.Optional.empty());
	
	    assertThrows(
	            ShortUrlNotFoundException.class,
	            () -> urlMappingService.updateOriginalUrl(shortCode, newUrl)
	    );
	
	    verify(urlMappingRepository).findByShortCode(shortCode);
	    verify(urlMappingRepository, times(0)).save(any(UrlMapping.class));
	}

	@Test
	void updateOriginalUrlShouldRejectInvalidUrl() {
	
	    String shortCode = "aB7xK2pQ";
	    String invalidUrl = "ftp://example.com/file";
	
	    assertThrows(
	            IllegalArgumentException.class,
	            () -> urlMappingService.updateOriginalUrl(shortCode, invalidUrl)
	    );
	
	    verifyNoInteractions(urlMappingRepository);
	}

	
	@Test
	void updateOriginalUrlShouldRejectBlankShortCode() {
	
	    String blankShortCode = "  ";
	    String newUrl = "https://example.com/new-page";
	
	    assertThrows(
	            IllegalArgumentException.class,
	            () -> urlMappingService.updateOriginalUrl(
	                    blankShortCode, newUrl)
	    );
	
	    verifyNoInteractions(urlMappingRepository);
	}

@Test
void deleteShortUrlShouldDeleteExistingMapping() {
    String shortCode = "abc12345";
    UrlMapping mapping = new UrlMapping(
            shortCode, "https://example.com/page");

    when(urlMappingRepository.findByShortCode(shortCode))
            .thenReturn(Optional.of(mapping));

    urlMappingService.deleteShortUrl(shortCode);

    verify(urlMappingRepository).delete(mapping);
}

@Test
void deleteShortUrlShouldThrowExceptionWhenShortCodeDoesNotExist() {
    String shortCode = "unknown1";

    when(urlMappingRepository.findByShortCode(shortCode))
            .thenReturn(Optional.empty());

    assertThrows(ShortUrlNotFoundException.class,
            () -> urlMappingService.deleteShortUrl(shortCode));

    verify(urlMappingRepository, never()).delete(any(UrlMapping.class));
}

		@Test
		void deleteShortUrlShouldRejectBlankShortCode() {
		    assertThrows(IllegalArgumentException.class,
		            () -> urlMappingService.deleteShortUrl(" "));
		
		    verify(urlMappingRepository, never()).findByShortCode(anyString());
		}
		
		@Test
		void deleteShortUrlShouldRejectNullShortCode() {
		    assertThrows(IllegalArgumentException.class,
		            () -> urlMappingService.deleteShortUrl(null));
		
		    verify(urlMappingRepository, never()).findByShortCode(any());
		}
		

@Test
void getOriginalUrlShouldReturnMappingWhenNotExpired() {
    String shortCode = "valid123";
    UrlMapping mapping = new UrlMapping(
            shortCode, "https://example.com/page");
    mapping.setExpiresAt(LocalDateTime.now().plusHours(1));

    when(urlMappingRepository.findByShortCode(shortCode))
            .thenReturn(Optional.of(mapping));

    UrlMapping result = urlMappingService.getOriginalUrl(shortCode);

    assertEquals(mapping, result);
}

@Test
void getOriginalUrlShouldThrowExceptionWhenExpired() {
    String shortCode = "expired1";
    UrlMapping mapping = new UrlMapping(
            shortCode, "https://example.com/page");
    mapping.setExpiresAt(LocalDateTime.now().minusMinutes(1));

    when(urlMappingRepository.findByShortCode(shortCode))
            .thenReturn(Optional.of(mapping));

    assertThrows(ShortUrlNotFoundException.class,
            () -> urlMappingService.getOriginalUrl(shortCode));
}

@Test
void getOriginalUrlShouldReturnMappingWhenExpirationIsNotSet() {
    String shortCode = "noexpiry";
    UrlMapping mapping = new UrlMapping(
            shortCode, "https://example.com/page");

    when(urlMappingRepository.findByShortCode(shortCode))
            .thenReturn(Optional.of(mapping));

    UrlMapping result = urlMappingService.getOriginalUrl(shortCode);

    assertEquals(mapping, result);
}

@Test
void createShortUrlShouldRejectPastExpirationDate() {

    LocalDateTime pastExpiration =
            LocalDateTime.now().minusDays(1);

    assertThrows(
            IllegalArgumentException.class,
            () -> urlMappingService.createShortUrl(
                    "https://example.com/page",
                    pastExpiration)
    );

    verifyNoInteractions(urlMappingRepository);
}


@Test
void cleanupExpiredUrlsShouldDeleteUsingThirtyDayRetentionPeriod() {

    LocalDateTime beforeTest = LocalDateTime.now();

    when(urlMappingRepository.deleteExpiredUrlsBefore(any(LocalDateTime.class)))
            .thenReturn(3);

    int deletedCount = urlMappingService.cleanupExpiredUrls();

    assertEquals(3, deletedCount);

    org.mockito.ArgumentCaptor<LocalDateTime> cutoffCaptor =
            org.mockito.ArgumentCaptor.forClass(LocalDateTime.class);

    verify(urlMappingRepository)
            .deleteExpiredUrlsBefore(cutoffCaptor.capture());

    LocalDateTime actualCutoff = cutoffCaptor.getValue();
    LocalDateTime expectedCutoff = beforeTest.minusDays(30);
    System.out.println("Actual cutoff   : " + actualCutoff);
    System.out.println("Expected cutoff : " + beforeTest.minusDays(30));
    System.out.println("Retention field : "
            + ReflectionTestUtils.getField(urlMappingService, "retentionDays"));

    assertTrue(
            !actualCutoff.isBefore(expectedCutoff.minusSeconds(1))
                    && !actualCutoff.isAfter(expectedCutoff.plusSeconds(1)),
            "Cleanup cutoff should be approximately 30 days before execution"
    );
}


@Test
void createShortUrlShouldAcceptValidCustomAlias() {
    String alias = "google123";
    String originalUrl = "https://www.google.com";

    when(urlMappingRepository.existsByShortCode(alias))
            .thenReturn(false);

    UrlMapping savedMapping = new UrlMapping(alias, originalUrl);

    when(urlMappingRepository.save(any(UrlMapping.class)))
            .thenReturn(savedMapping);

    UrlMapping result = urlMappingService.createShortUrl(
            originalUrl, null, alias);

    assertEquals(alias, result.getShortCode());
    assertEquals(originalUrl, result.getOriginalUrl());

    verify(urlMappingRepository).existsByShortCode(alias);
    verify(urlMappingRepository).save(any(UrlMapping.class));
}

@Test
void createShortUrlShouldRejectDuplicateCustomAlias() {
    String alias = "google123";

    when(urlMappingRepository.existsByShortCode(alias))
            .thenReturn(true);

    assertThrows(IllegalArgumentException.class,
            () -> urlMappingService.createShortUrl(
                    "https://www.google.com", null, alias));

    verify(urlMappingRepository).existsByShortCode(alias);
    verify(urlMappingRepository, never()).save(any(UrlMapping.class));
}

@Test
void createShortUrlShouldRejectCustomAliasLongerThanTenCharacters() {
    String alias = "google12345";

    assertThrows(IllegalArgumentException.class,
            () -> urlMappingService.createShortUrl(
                    "https://www.google.com", null, alias));

    verifyNoInteractions(urlMappingRepository);
}

@Test
void createShortUrlShouldRejectCustomAliasWithSpecialCharacters() {
    String alias = "google_123";

    assertThrows(IllegalArgumentException.class,
            () -> urlMappingService.createShortUrl(
                    "https://www.google.com", null, alias));

    verifyNoInteractions(urlMappingRepository);
}

@Test
void createShortUrlShouldRejectReservedCustomAlias() {
    String alias = "API";

    assertThrows(IllegalArgumentException.class,
            () -> urlMappingService.createShortUrl(
                    "https://www.google.com", null, alias));

    verifyNoInteractions(urlMappingRepository);
}

}
