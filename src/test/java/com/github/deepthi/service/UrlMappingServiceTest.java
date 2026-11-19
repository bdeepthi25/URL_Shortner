package com.github.deepthi.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.github.deepthi.entity.UrlMapping;
import com.github.deepthi.exception.ShortUrlNotFoundException;
import com.github.deepthi.repository.UrlClickRepository;
import com.github.deepthi.repository.UrlMappingRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import com.github.deepthi.dto.CachedUrlMapping;

@ExtendWith(MockitoExtension.class)
public class UrlMappingServiceTest {



	   @Mock
	    private UrlMappingRepository urlMappingRepository;
	   @Mock
	   private UrlClickRepository urlClickRepository;


	   @Mock
	   private RedisTemplate<String, CachedUrlMapping> urlMappingRedisTemplate;

	   @Mock
	   private ValueOperations<String, CachedUrlMapping> valueOperations;

	    private UrlMappingService urlMappingService;

	    @Autowired
	    private MockMvc mockMvc;

//	    @BeforeEach
//	    void setUp() {
//	        urlMappingService =
//	                new UrlMappingService(urlMappingRepository, urlClickRepository);
//	        ReflectionTestUtils.setField(
//		            urlMappingService,
//		            "retentionDays",
//		            30
//		    );
//	    }

	    @BeforeEach
	    void setUp() {
	        urlMappingService = new UrlMappingService(
	                urlMappingRepository,
	                urlClickRepository,
	                urlMappingRedisTemplate
	        );

//	        when(urlMappingRedisTemplate.opsForValue())
//	                .thenReturn(valueOperations);
	        Mockito.lenient()
	        .when(urlMappingRedisTemplate.opsForValue())
	        .thenReturn(valueOperations);

	        ReflectionTestUtils.setField(
	                urlMappingService,
	                "retentionDays",
	                30
	        );

	        ReflectionTestUtils.setField(
	                urlMappingService,
	                "cacheTtlSeconds",
	                600L
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

//	    @Test
//	    void createShortUrlShouldRetryWhenCodeAlreadyExists() {
//
//	        String originalUrl = "https://example.com";
//
//	        when(urlMappingRepository.existsByShortCode(anyString()))
//	                .thenReturn(true, false);
//
//	        when(urlMappingRepository.save(any(UrlMapping.class)))
//	                .thenAnswer(invocation -> invocation.getArgument(0));
//
//	        UrlMapping result =
//	                urlMappingService.createShortUrl(originalUrl, null);
//
//	        assertNotNull(result);
//	        assertEquals(originalUrl, result.getOriginalUrl());
//
//	        verify(urlMappingRepository, times(2))
//	                .existsByShortCode(anyString());
//
//	        verify(urlMappingRepository).save(any(UrlMapping.class));
//	    }

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
void getOriginalUrlShouldFetchFromDatabaseAndCacheWhenCacheMiss() {

    String shortCode = "aB7xK2pQ";
    String originalUrl = "https://example.com/products";

    UrlMapping mapping = new UrlMapping(shortCode, originalUrl);

    when(urlMappingRepository.findByShortCode(shortCode))
            .thenReturn(Optional.of(mapping));

    UrlMapping result = urlMappingService.getOriginalUrl(shortCode);

    assertNotNull(result);
    assertEquals(shortCode, result.getShortCode());
    assertEquals(originalUrl, result.getOriginalUrl());

    // Verify PostgreSQL was queried.
    verify(urlMappingRepository).findByShortCode(shortCode);

    // Verify the result was stored in Redis.
    verify(valueOperations).set(
            Mockito.eq("url:" + shortCode),
            any(CachedUrlMapping.class),
            Mockito.eq(java.time.Duration.ofSeconds(600))
    );
}

@Test
void getOriginalUrlShouldReturnCachedMappingWithoutQueryingDatabase() {

    String shortCode = "aB7xK2pQ";
    String originalUrl = "https://example.com/products";

    LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
    LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);

    CachedUrlMapping cached = new CachedUrlMapping(
            1L,
            shortCode,
            originalUrl,
            createdAt,
            expiresAt
    );

    when(valueOperations.get("url:" + shortCode))
            .thenReturn(cached);

    UrlMapping result = urlMappingService.getOriginalUrl(shortCode);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals(shortCode, result.getShortCode());
    assertEquals(originalUrl, result.getOriginalUrl());
    assertEquals(createdAt, result.getCreatedAt());
    assertEquals(expiresAt, result.getExpiresAt());

    verify(valueOperations).get("url:" + shortCode);

    verify(urlMappingRepository, never())
            .findByShortCode(anyString());

    verify(urlMappingRepository, never())
            .findById(Mockito.anyLong());
}

@Test
void getOriginalUrlShouldDeleteExpiredCachedMappingAndThrowException() {

    String shortCode = "aB7xK2pQ";
    String originalUrl = "https://example.com/products";

    LocalDateTime createdAt = LocalDateTime.now().minusDays(40);
    LocalDateTime expiresAt = LocalDateTime.now().minusDays(1);

    CachedUrlMapping cached = new CachedUrlMapping(
            1L,
            shortCode,
            originalUrl,
            createdAt,
            expiresAt
    );

    when(valueOperations.get("url:" + shortCode))
            .thenReturn(cached);

    assertThrows(
            ShortUrlNotFoundException.class,
            () -> urlMappingService.getOriginalUrl(shortCode)
    );

    verify(valueOperations).get("url:" + shortCode);

    verify(urlMappingRedisTemplate).delete("url:" + shortCode);

    verify(urlMappingRepository, never())
            .findByShortCode(anyString());
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


//	@Test
//	void updateOriginalUrlShouldRejectBlankShortCode() {
//
//	    String blankShortCode = "  ";
//	    String newUrl = "https://example.com/new-page";
//
//	    assertThrows(
//	            IllegalArgumentException.class,
//	            () -> urlMappingService.updateOriginalUrl(
//	                    blankShortCode, newUrl)
//	    );
//
//	    verifyNoInteractions(urlMappingRepository);
//	}

@Test
void deleteShortUrlShouldDeleteExistingMapping() {
    String shortCode = "abc12345";
    UrlMapping mapping = new UrlMapping(
            shortCode, "https://example.com/page");

    when(urlMappingRepository.findByShortCode(shortCode))
            .thenReturn(Optional.of(mapping));

    urlMappingService.deleteShortUrl(shortCode);

//    verify(urlMappingRepository).delete(mapping);

    InOrder inOrder = Mockito.inOrder(
            urlClickRepository,
            urlMappingRepository);

    inOrder.verify(urlClickRepository)
            .deleteByUrlMapping_ShortCode(shortCode);

    inOrder.verify(urlMappingRepository).delete(mapping);
}

@Test
void deleteShortUrlShouldThrowExceptionWhenShortCodeDoesNotExist() {
    String shortCode = "unknown1";

    when(urlMappingRepository.findByShortCode(shortCode))
            .thenReturn(Optional.empty());

    assertThrows(ShortUrlNotFoundException.class,
            () -> urlMappingService.deleteShortUrl(shortCode));

    verify(urlMappingRepository, never()).delete(any(UrlMapping.class));

    verify(urlClickRepository, never())
    .deleteByUrlMapping_ShortCode(shortCode);
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


//@Test
//void cleanupExpiredUrlsShouldDeleteUsingThirtyDayRetentionPeriod() {
//
//    LocalDateTime beforeTest = LocalDateTime.now();
//
//    when(urlMappingRepository.deleteExpiredUrlsBefore(any(LocalDateTime.class)))
//            .thenReturn(3);
//
//    int deletedCount = urlMappingService.cleanupExpiredUrls();
//
//    assertEquals(3, deletedCount);
//
//    org.mockito.ArgumentCaptor<LocalDateTime> cutoffCaptor =
//            org.mockito.ArgumentCaptor.forClass(LocalDateTime.class);
//
//    verify(urlMappingRepository)
//            .deleteExpiredUrlsBefore(cutoffCaptor.capture());
//
//    LocalDateTime actualCutoff = cutoffCaptor.getValue();
//    LocalDateTime expectedCutoff = beforeTest.minusDays(30);
//    System.out.println("Actual cutoff   : " + actualCutoff);
//    System.out.println("Expected cutoff : " + beforeTest.minusDays(30));
//    System.out.println("Retention field : "
//            + ReflectionTestUtils.getField(urlMappingService, "retentionDays"));
//
//    assertTrue(
//            !actualCutoff.isBefore(expectedCutoff.minusSeconds(1))
//                    && !actualCutoff.isAfter(expectedCutoff.plusSeconds(1)),
//            "Cleanup cutoff should be approximately 30 days before execution"
//    );
//}


@Test
void cleanupExpiredUrlsShouldDeleteUsingThirtyDayRetentionPeriod() {

    LocalDateTime beforeTest = LocalDateTime.now();

    when(urlMappingRepository.deleteExpiredUrlsBefore(any(LocalDateTime.class)))
            .thenReturn(3);

    int deletedCount = urlMappingService.cleanupExpiredUrls();

    assertEquals(3, deletedCount);

    ArgumentCaptor<LocalDateTime> cutoffCaptor =
            ArgumentCaptor.forClass(LocalDateTime.class);

    InOrder inOrder = Mockito.inOrder(
            urlClickRepository,
            urlMappingRepository
    );

    inOrder.verify(urlClickRepository)
            .deleteClicksForExpiredUrlsBefore(cutoffCaptor.capture());

    inOrder.verify(urlMappingRepository)
            .deleteExpiredUrlsBefore(cutoffCaptor.capture());

    LocalDateTime clickCutoff = cutoffCaptor.getAllValues().get(0);
    LocalDateTime urlCutoff = cutoffCaptor.getAllValues().get(1);

    LocalDateTime expectedCutoff = beforeTest.minusDays(30);

    assertEquals(clickCutoff, urlCutoff);

    assertTrue(
            !clickCutoff.isBefore(expectedCutoff.minusSeconds(1))
                    && !clickCutoff.isAfter(expectedCutoff.plusSeconds(1)),
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


@Test
void createShortUrlShouldHandleAliasRaceCondition() {
    String alias = "deepthi";

    when(urlMappingRepository.existsByShortCode(alias))
            .thenReturn(false);

    doThrow(new DataIntegrityViolationException(
            "Duplicate short code"))
            .when(urlMappingRepository)
            .save(any(UrlMapping.class));

    assertThrows(
            DataIntegrityViolationException.class,
            () -> urlMappingService.createShortUrl(
                    "https://www.google.com",
                    null,
                    alias));

    verify(urlMappingRepository).existsByShortCode(alias);
    verify(urlMappingRepository).save(any(UrlMapping.class));
}

@Test
void createShortUrlShouldPreserveExpirationDate() {

    String originalUrl = "https://example.com/products";
    LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

    when(urlMappingRepository.existsByShortCode(anyString()))
            .thenReturn(false);

    when(urlMappingRepository.save(any(UrlMapping.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    UrlMapping result =
            urlMappingService.createShortUrl(originalUrl, expiresAt);

    assertNotNull(result);
    assertEquals(originalUrl, result.getOriginalUrl());
    assertEquals(expiresAt, result.getExpiresAt());

    verify(urlMappingRepository).save(any(UrlMapping.class));
}


//@Test
//void deleteShortUrlShouldReturn404WhenShortCodeDoesNotExist()
//        throws Exception {
//
//    String shortCode = "unknown1";
//
//    org.mockito.Mockito.doThrow(
//            new ShortUrlNotFoundException(
//                    "Short URL not found for code: " + shortCode))
//            .when(urlMappingService)
//            .deleteShortUrl(shortCode);
//
//    mockMvc.perform(delete("/api/urls/" + shortCode))
//            .andExpect(status().isNotFound())
//            .andExpect(jsonPath("$.error")
//                    .value("Short URL Not Found"));
//
//    verify(urlMappingService).deleteShortUrl(shortCode);
//}


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
    assertNotNull(result.getShortCode());
    assertEquals(8, result.getShortCode().length());

    ArgumentCaptor<String> codeCaptor =
            ArgumentCaptor.forClass(String.class);

    verify(urlMappingRepository, times(2))
            .existsByShortCode(codeCaptor.capture());

    String firstCode = codeCaptor.getAllValues().get(0);
    String secondCode = codeCaptor.getAllValues().get(1);

    assertNotNull(firstCode);
    assertNotNull(secondCode);

    ArgumentCaptor<UrlMapping> mappingCaptor =
            ArgumentCaptor.forClass(UrlMapping.class);

    verify(urlMappingRepository).save(mappingCaptor.capture());

    assertEquals(secondCode, mappingCaptor.getValue().getShortCode());
}

@Test
void createShortUrlShouldRejectNullOriginalUrl() {
    assertThatThrownBy(() ->
            urlMappingService.createShortUrl(null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Original URL must not be empty.");
}

@Test
void createShortUrlShouldRejectBlankOriginalUrl() {
    assertThatThrownBy(() ->
            urlMappingService.createShortUrl("   ", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Original URL must not be empty.");
}

@Test
void createShortUrlShouldRejectUnsupportedUrlScheme() {
    assertThatThrownBy(() ->
            urlMappingService.createShortUrl("ftp://example.com", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Please provide a valid HTTP or HTTPS URL.");
}

@Test
void getOriginalUrlShouldRejectNullShortCode() {
    assertThatThrownBy(() ->
            urlMappingService.getOriginalUrl(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Short code must not be empty.");
}

@Test
void getOriginalUrlShouldRejectBlankShortCode() {
    assertThatThrownBy(() ->
            urlMappingService.getOriginalUrl(" "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Short code must not be empty.");
}

@Test
void updateOriginalUrlShouldRejectNullShortCode() {
    assertThatThrownBy(() ->
            urlMappingService.updateOriginalUrl(null, "https://example.com"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Short code must not be empty.");
}

@Test
void updateOriginalUrlShouldRejectBlankShortCode() {
    assertThatThrownBy(() ->
            urlMappingService.updateOriginalUrl(" ", "https://example.com"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Short code must not be empty.");
}

@Test
void createShortUrlShouldGenerateRandomCodeWhenCustomAliasIsBlank() {
    String originalUrl = "https://example.com";

    when(urlMappingRepository.existsByShortCode(anyString()))
            .thenReturn(false);

    when(urlMappingRepository.save(any(UrlMapping.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    UrlMapping result = urlMappingService.createShortUrl(
            originalUrl, null, " ");

    assertNotNull(result);
    assertEquals(originalUrl, result.getOriginalUrl());
    assertNotNull(result.getShortCode());
    assertEquals(8, result.getShortCode().length());

    verify(urlMappingRepository).existsByShortCode(anyString());
    verify(urlMappingRepository).save(any(UrlMapping.class));
}

@Test
void getAllUrlsShouldReturnPaginatedMappings() {
    Pageable pageable = PageRequest.of(0, 10);

    List<UrlMapping> mappings = List.of(
            new UrlMapping("abc12345", "https://example.com"),
            new UrlMapping("xyz67890", "https://openai.com")
    );

    Page<UrlMapping> expectedPage =
            new PageImpl<>(mappings, pageable, mappings.size());

    when(urlMappingRepository.findAll(pageable))
            .thenReturn(expectedPage);

    Page<UrlMapping> result = urlMappingService.getAllUrls(pageable);

    assertEquals(2, result.getTotalElements());
    assertEquals(mappings, result.getContent());

    verify(urlMappingRepository).findAll(pageable);
}

@Test
void updateOriginalUrlShouldInvalidateRedisCache() {

    String shortCode = "aB7xK2pQ";
    String oldUrl = "https://example.com/old";
    String newUrl = "https://example.com/new";

    UrlMapping existingMapping = new UrlMapping(shortCode, oldUrl);

    when(urlMappingRepository.findByShortCode(shortCode))
            .thenReturn(Optional.of(existingMapping));

    when(urlMappingRepository.save(any(UrlMapping.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    UrlMapping result =
            urlMappingService.updateOriginalUrl(shortCode, newUrl);

    assertNotNull(result);
    assertEquals(newUrl, result.getOriginalUrl());

    verify(urlMappingRepository).save(any(UrlMapping.class));

    verify(urlMappingRedisTemplate).delete("url:" + shortCode);
}

@Test
void deleteShortUrlShouldInvalidateRedisCache() {

    String shortCode = "aB7xK2pQ";

    UrlMapping mapping = new UrlMapping(shortCode,
            "https://example.com/products");

    when(urlMappingRepository.findByShortCode(shortCode))
            .thenReturn(Optional.of(mapping));

    urlMappingService.deleteShortUrl(shortCode);

    verify(urlMappingRepository).findByShortCode(shortCode);

    verify(urlClickRepository)
            .deleteByUrlMapping_ShortCode(shortCode);

    verify(urlMappingRepository).delete(mapping);

    verify(urlMappingRedisTemplate).delete("url:" + shortCode);
}

@Test
void updateOriginalUrlShouldUpdateCachedMappingAndPreserveMetadata() {

    String shortCode = "aB7xK2pQ";
    String oldUrl = "https://example.com/old";
    String newUrl = "https://example.com/new";

    LocalDateTime createdAt = LocalDateTime.now().minusDays(5);
    LocalDateTime expiresAt = LocalDateTime.now().plusDays(10);

    CachedUrlMapping cached = new CachedUrlMapping(
            1L,
            shortCode,
            oldUrl,
            createdAt,
            expiresAt
    );

    when(valueOperations.get("url:" + shortCode))
            .thenReturn(cached);

    when(urlMappingRepository.save(any(UrlMapping.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    UrlMapping result =
            urlMappingService.updateOriginalUrl(shortCode, newUrl);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals(newUrl, result.getOriginalUrl());
    assertEquals(createdAt, result.getCreatedAt());
    assertEquals(expiresAt, result.getExpiresAt());

    verify(valueOperations).get("url:" + shortCode);
    verify(urlMappingRepository).save(any(UrlMapping.class));
    verify(urlMappingRedisTemplate).delete("url:" + shortCode);
}

@Test
void deleteShortUrlShouldDeleteCachedMappingAndDatabaseRecords() {

    String shortCode = "aB7xK2pQ";

    UrlMapping mapping = new UrlMapping(
            1L,
            shortCode,
            "https://example.com/products",
            LocalDateTime.now().minusDays(5),
            LocalDateTime.now().plusDays(10)
    );

    when(urlMappingRepository.findByShortCode(shortCode))
            .thenReturn(Optional.of(mapping));

    urlMappingService.deleteShortUrl(shortCode);

    verify(urlMappingRepository).findByShortCode(shortCode);

    verify(urlClickRepository)
            .deleteByUrlMapping_ShortCode(shortCode);

    verify(urlMappingRepository).delete(mapping);

    verify(urlMappingRedisTemplate).delete("url:" + shortCode);
}

}
