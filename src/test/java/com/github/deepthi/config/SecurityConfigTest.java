
package com.github.deepthi.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.github.deepthi.controller.UrlShortenerController;
import com.github.deepthi.entity.UrlMapping;
import com.github.deepthi.service.UrlClickService;
import com.github.deepthi.service.UrlMappingService;

@WebMvcTest(UrlShortenerController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "ADMIN_USERNAME=testadmin",
        "ADMIN_PASSWORD=testpassword"
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlMappingService urlMappingService;

    @MockitoBean
    private UrlClickService urlClickService;

    @Test
    void publicRedirectShouldWorkWithoutAuthentication() throws Exception {

    	UrlMapping mapping = new UrlMapping(
    	        "test1234",
    	        "https://example.com"
    	);

    	when(urlMappingService.getOriginalUrl("test1234"))
    	        .thenReturn(mapping);
        mockMvc.perform(get("/test1234"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com"));
    }

    @Test
    void adminEndpointShouldRejectUnauthenticatedRequest() throws Exception {

        mockMvc.perform(get("/api/urls"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpointShouldAllowAuthenticatedAdmin() throws Exception {

        when(urlMappingService.getAllUrls(any(Pageable.class)))
                .thenReturn(Page.<UrlMapping>empty());

        String credentials = Base64.getEncoder().encodeToString(
                "testadmin:testpassword".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(get("/api/urls")
                .header("Authorization", "Basic " + credentials))
                .andExpect(status().isOk());
    }
}
