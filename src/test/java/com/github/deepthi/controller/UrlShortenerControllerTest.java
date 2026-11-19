package com.github.deepthi.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.github.deepthi.dto.DailyClickStatsResponse;
import com.github.deepthi.entity.UrlMapping;
import com.github.deepthi.exception.ShortUrlNotFoundException;
import com.github.deepthi.service.UrlClickService;
import com.github.deepthi.service.UrlMappingService;

@WebMvcTest(UrlShortenerController.class)
public class UrlShortenerControllerTest {
	  @Autowired
	    private MockMvc mockMvc;

	    @MockitoBean
	    private UrlMappingService urlMappingService;
	    @MockitoBean
	    private UrlClickService urlClickService;

	    @Test
	    void createShortUrlShouldReturn201() throws Exception {

	        String originalUrl = "https://example.com/products";
	        String shortCode = "aB7xK2pQ";

	        UrlMapping mapping =
	                new UrlMapping(shortCode, originalUrl);

	        when(urlMappingService.createShortUrl(originalUrl, null ,null))
	                .thenReturn(mapping);

	        mockMvc.perform(post("/api/urls")
	                .contentType(MediaType.APPLICATION_JSON)
	                .content("""
	                        {
	                            "originalUrl": "https://example.com/products"
	                        }
	                        """))
	                .andExpect(status().isCreated())
	                .andExpect(jsonPath("$.shortCode").value(shortCode))
	                .andExpect(jsonPath("$.originalUrl").value(originalUrl))
	                .andExpect(jsonPath("$.shortUrl")
	                        .value(org.hamcrest.Matchers.endsWith("/" + shortCode)));

	        verify(urlMappingService).createShortUrl(originalUrl, null, null);
	    }

	    @Test
	    void redirectShouldReturn302WhenShortCodeExists() throws Exception {

	        String shortCode = "aB7xK2pQ";
	        String originalUrl = "https://example.com/products";

	        UrlMapping mapping =
	                new UrlMapping(shortCode, originalUrl);

	        when(urlMappingService.getOriginalUrl(shortCode))
	                .thenReturn(mapping);

	        mockMvc.perform(get("/" + shortCode))
	                .andExpect(status().isFound())
	                .andExpect(header().string("Location", originalUrl));

	        verify(urlMappingService).getOriginalUrl(shortCode);
	        verify(urlClickService).recordClick(mapping);
	    }

	    @Test
	    void redirectShouldReturn404WhenShortCodeDoesNotExist()
	            throws Exception {

	        String shortCode = "unknown1";

	        when(urlMappingService.getOriginalUrl(shortCode))
	                .thenThrow(new ShortUrlNotFoundException(
	                        "Short URL not found for code: " + shortCode));

	        mockMvc.perform(get("/" + shortCode))
	                .andExpect(status().isNotFound())
	                .andExpect(jsonPath("$.error")
	                        .value("Short URL Not Found"))
	                .andExpect(jsonPath("$.message")
	                        .value("Short URL not found for code: " + shortCode));
	    }

	    @Test
	    void createShortUrlShouldReturn400ForInvalidUrl()
	            throws Exception {

	        String invalidUrl = "not-a-valid-url";

	        when(urlMappingService.createShortUrl(invalidUrl,null, null))
	                .thenThrow(new IllegalArgumentException(
	                        "Please provide a valid HTTP or HTTPS URL."));

	        mockMvc.perform(post("/api/urls")
	                .contentType(MediaType.APPLICATION_JSON)
	                .content("""
	                        {
	                            "originalUrl": "not-a-valid-url"
	                        }
	                        """))
	                .andExpect(status().isBadRequest())
	                .andExpect(jsonPath("$.error").value("Bad Request"))
	                .andExpect(jsonPath("$.message")
	                        .value("Please provide a valid HTTP or HTTPS URL."));
	    }
	    @Test
	    void createShortUrlShouldReturn400WhenOriginalUrlIsMissing()
	            throws Exception {

	        mockMvc.perform(post("/api/urls")
	                .contentType(MediaType.APPLICATION_JSON)
	                .content("{}"))
	                .andExpect(status().isBadRequest());

	        org.mockito.Mockito.verifyNoInteractions(urlMappingService);
	    }
	    @Test
	    void createShortUrlShouldReturn400WhenOriginalUrlIsBlank()
	            throws Exception {

	        mockMvc.perform(post("/api/urls")
	                .contentType(MediaType.APPLICATION_JSON)
	                .content("""
	                        {
	                            "originalUrl": "   "
	                        }
	                        """))
	                .andExpect(status().isBadRequest());

	        org.mockito.Mockito.verifyNoInteractions(urlMappingService);
	    }

@Test
void createShortUrlShouldReturnCustomValidationErrorForBlankUrl()
        throws Exception {

    mockMvc.perform(post("/api/urls")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                        "originalUrl": "   "
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message")
                    .value("Original URL must not be empty."));

    org.mockito.Mockito.verifyNoInteractions(urlMappingService);
}

@Test
void createShortUrlShouldPassCustomAliasToService() throws Exception {

    String originalUrl = "https://example.com/products";
    String customAlias = "products1";

    UrlMapping mapping = new UrlMapping(customAlias, originalUrl);

    when(urlMappingService.createShortUrl(
            originalUrl, null, customAlias))
            .thenReturn(mapping);

    mockMvc.perform(post("/api/urls")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                        "originalUrl": "https://example.com/products",
                        "customAlias": "products1"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.shortCode").value(customAlias))
            .andExpect(jsonPath("$.originalUrl").value(originalUrl));

    verify(urlMappingService)
            .createShortUrl(originalUrl, null, customAlias);
}

@Test
void createShortUrlShouldRejectInvalidCustomAlias() throws Exception {

    String originalUrl = "https://www.google.com";
    String invalidAlias = "google_123";

    when(urlMappingService.createShortUrl(
            originalUrl, null, invalidAlias))
            .thenThrow(new IllegalArgumentException(
                    "Custom alias can contain only letters and numbers."));

    mockMvc.perform(post("/api/urls")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                        "originalUrl": "https://www.google.com",
                        "customAlias": "google_123"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message")
                    .value("Custom alias can contain only letters and numbers."));

    verify(urlMappingService)
            .createShortUrl(originalUrl, null, invalidAlias);
}


@Test
void getAllUrlsShouldReturnPaginatedResults() throws Exception {

    UrlMapping mapping = new UrlMapping(
            "aB7xK2pQ",
            "https://example.com/products");

    when(urlMappingService.getAllUrls(any(Pageable.class)))
            .thenReturn(new PageImpl<>(
                    List.of(mapping),
                    PageRequest.of(1, 2),
                    5));

    mockMvc.perform(get("/api/urls")
            .param("page", "1")
            .param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].shortCode")
                    .value("aB7xK2pQ"))
            .andExpect(jsonPath("$.content[0].originalUrl")
                    .value("https://example.com/products"))
            .andExpect(jsonPath("$.content[0].shortUrl")
                    .value(org.hamcrest.Matchers.endsWith("/aB7xK2pQ")))
            .andExpect(jsonPath("$.page.number").value(1))
            .andExpect(jsonPath("$.page.size").value(2))
            .andExpect(jsonPath("$.page.totalElements").value(5))
            .andExpect(jsonPath("$.page.totalPages").value(3));
//    verify(urlMappingService).getAllUrls(PageRequest.of(1, 2));
    verify(urlMappingService).getAllUrls(
            PageRequest.of(1, 2, Sort.by(Sort.Direction.ASC, "id"))
    );
}

@Test
void getAllUrlsShouldUseDefaultPagination() throws Exception {

    when(urlMappingService.getAllUrls(any(Pageable.class)))
            .thenReturn(new PageImpl<>(
                    List.of(),
                    PageRequest.of(0, 10),
                    0));

    mockMvc.perform(get("/api/urls"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page.number").value(0))
            .andExpect(jsonPath("$.page.size").value(10))
            .andExpect(jsonPath("$.page.totalElements").value(0));

    verify(urlMappingService).getAllUrls(
            PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"))
    );
}

@Test
void getAllUrlsShouldReturn400ForNegativePage() throws Exception {

    mockMvc.perform(get("/api/urls")
            .param("page", "-1")
            .param("size", "10"))
            .andExpect(status().isBadRequest());

    verifyNoInteractions(urlMappingService);
}

@Test
void getAllUrlsShouldReturn400ForZeroSize() throws Exception {

    mockMvc.perform(get("/api/urls")
            .param("page", "0")
            .param("size", "0"))
            .andExpect(status().isBadRequest());

    verifyNoInteractions(urlMappingService);
}

@Test
void getAllUrlsShouldReturn400WhenSizeExceeds100() throws Exception {

    mockMvc.perform(get("/api/urls")
            .param("page", "0")
            .param("size", "101"))
            .andExpect(status().isBadRequest());

    verifyNoInteractions(urlMappingService);
}


@Test
void redirectShouldReturn404WhenShortCodeIsExpired()
        throws Exception {

    String shortCode = "expired1";

    when(urlMappingService.getOriginalUrl(shortCode))
            .thenThrow(new ShortUrlNotFoundException(
                    "Short URL has expired: " + shortCode));

    mockMvc.perform(get("/" + shortCode))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error")
                    .value("Short URL Not Found"));

    verify(urlMappingService).getOriginalUrl(shortCode);
}

@Test
void deleteShortUrlShouldReturn204WhenShortCodeExists() throws Exception {
    String shortCode = "aB7xK2pQ";

    mockMvc.perform(delete("/api/urls/" + shortCode))
            .andExpect(status().isNoContent());

    verify(urlMappingService).deleteShortUrl(shortCode);
}

@Test
void deleteShortUrlShouldReturn404WhenShortCodeDoesNotExist()
        throws Exception {
    String shortCode = "unknown1";

    org.mockito.Mockito.doThrow(
            new ShortUrlNotFoundException(
                    "Short URL not found for code: " + shortCode))
            .when(urlMappingService)
            .deleteShortUrl(shortCode);

    mockMvc.perform(delete("/api/urls/" + shortCode))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error")
                    .value("Short URL Not Found"));

    verify(urlMappingService).deleteShortUrl(shortCode);
}


@Test
void updateShortUrlShouldReturn200WhenShortCodeExists()
        throws Exception {

    String shortCode = "aB7xK2pQ";
    String updatedUrl = "https://example.com/updated";

    UrlMapping mapping =
            new UrlMapping(shortCode, updatedUrl);

    when(urlMappingService.updateOriginalUrl(
            shortCode, updatedUrl))
            .thenReturn(mapping);

    mockMvc.perform(put("/api/urls/{shortCode}", shortCode)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                        "originalUrl": "https://example.com/updated"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.shortCode").value(shortCode))
            .andExpect(jsonPath("$.originalUrl").value(updatedUrl));

    verify(urlMappingService)
            .updateOriginalUrl(shortCode, updatedUrl);
}


@Test
void updateShortUrlShouldReturn404WhenShortCodeDoesNotExist()
        throws Exception {

    String shortCode = "unknown1";
    String updatedUrl = "https://example.com/updated";

    when(urlMappingService.updateOriginalUrl(
            shortCode, updatedUrl))
            .thenThrow(new ShortUrlNotFoundException(
                    "Short URL not found for code: " + shortCode));

    mockMvc.perform(put("/api/urls/{shortCode}", shortCode)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                        "originalUrl": "https://example.com/updated"
                    }
                    """))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error")
                    .value("Short URL Not Found"));

    verify(urlMappingService)
            .updateOriginalUrl(shortCode, updatedUrl);
}

@Test
void getUrlClickStatsShouldReturnTotalClicks() throws Exception {
    String shortCode = "click001";

    when(urlClickService.getTotalClicks(shortCode)).thenReturn(5L);

    mockMvc.perform(get("/api/urls/{shortCode}/stats", shortCode))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.shortCode").value(shortCode))
            .andExpect(jsonPath("$.totalClicks").value(5));

    verify(urlClickService).getTotalClicks(shortCode);
    verify(urlClickService, never()).recordClick(any(UrlMapping.class));
}

@Test
void getUrlClickStatsShouldReturnNotFoundForUnknownShortCode()
        throws Exception {

    String shortCode = "unknown123";

    when(urlMappingService.getOriginalUrl(shortCode))
    .thenThrow(new ShortUrlNotFoundException(
            "Short URL not found for code: " + shortCode));

    mockMvc.perform(get("/api/urls/{shortCode}/stats", shortCode))
            .andExpect(status().isNotFound());

    verify(urlMappingService).getOriginalUrl(shortCode);

    verify(urlClickService, never()).getTotalClicks(shortCode);
}


@Test
void getUrlClickStatsShouldReturnDailyClicks() throws Exception {
    String shortCode = "daily001";

    when(urlClickService.getTotalClicks(shortCode))
            .thenReturn(3L);

    when(urlClickService.getDailyClicks(shortCode))
            .thenReturn(List.of(
                    new DailyClickStatsResponse(
                            java.time.LocalDate.of(2026, 10, 25),
                            3L)));

    mockMvc.perform(get("/api/urls/{shortCode}/stats", shortCode))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.shortCode").value(shortCode))
            .andExpect(jsonPath("$.totalClicks").value(3))
            .andExpect(jsonPath("$.dailyClicks").isArray())
            .andExpect(jsonPath("$.dailyClicks.length()").value(1))
            .andExpect(jsonPath("$.dailyClicks[0].date")
                    .value("2026-10-25"))
            .andExpect(jsonPath("$.dailyClicks[0].clicks").value(3));

    verify(urlClickService).getTotalClicks(shortCode);
    verify(urlClickService).getDailyClicks(shortCode);
}


@Test
void getUrlClickStatsShouldReturnZeroClicksWhenUrlHasNoClicks()
        throws Exception {

    String shortCode = "zero0001";

    when(urlClickService.getTotalClicks(shortCode))
            .thenReturn(0L);

    when(urlClickService.getDailyClicks(shortCode))
            .thenReturn(List.of());

    mockMvc.perform(get("/api/urls/{shortCode}/stats", shortCode))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.shortCode").value(shortCode))
            .andExpect(jsonPath("$.totalClicks").value(0))
            .andExpect(jsonPath("$.dailyClicks").isArray())
            .andExpect(jsonPath("$.dailyClicks").isEmpty());

    verify(urlClickService).getTotalClicks(shortCode);
    verify(urlClickService).getDailyClicks(shortCode);
    verify(urlClickService, never()).recordClick(any(UrlMapping.class));
}


}
