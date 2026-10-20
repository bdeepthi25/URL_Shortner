package com.github.deepthi.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.github.deepthi.entity.UrlMapping;
import com.github.deepthi.exception.ShortUrlNotFoundException;
import com.github.deepthi.service.UrlMappingService;

@WebMvcTest(UrlShortenerController.class)
public class UrlShortenerControllerTest {
	  @Autowired
	    private MockMvc mockMvc;

	    @MockitoBean
	    private UrlMappingService urlMappingService;

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
            .andExpect(jsonPath("$.number").value(1))
            .andExpect(jsonPath("$.size").value(2))
            .andExpect(jsonPath("$.totalElements").value(5))
            .andExpect(jsonPath("$.totalPages").value(3));

    verify(urlMappingService).getAllUrls(PageRequest.of(1, 2));
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
            .andExpect(jsonPath("$.number").value(0))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.totalElements").value(0));

    verify(urlMappingService).getAllUrls(PageRequest.of(0, 10));
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



}
