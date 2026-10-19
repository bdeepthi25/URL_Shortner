package com.github.deepthi.repository;

import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import com.github.deepthi.entity.UrlMapping;

@DataJpaTest
@AutoConfigureTestDatabase(
	    replace = AutoConfigureTestDatabase.Replace.NONE
	)
public class UrlMappingRepositoryTest {


	 @Autowired
	    private UrlMappingRepository urlMappingRepository;

	    @Test
	    void shouldSaveAndFindUrlMappingByShortCode() {

	        UrlMapping urlMapping = new UrlMapping(
	                "abc123",
	                "https://example.com/my-page"
	        );

	        UrlMapping savedMapping = urlMappingRepository.save(urlMapping);

	        Optional<UrlMapping> result =
	                urlMappingRepository.findByShortCode("abc123");

	        assertThat(savedMapping.getId()).isNotNull();
	        assertThat(result).isPresent();
	        assertThat(result.get().getOriginalUrl())
	                .isEqualTo("https://example.com/my-page");
	    }

	    @Test
	    void shouldReturnTrueWhenShortCodeExists() {

	        UrlMapping urlMapping = new UrlMapping(
	                "xyz789",
	                "https://example.com"
	        );

	        urlMappingRepository.save(urlMapping);

	        boolean exists =
	                urlMappingRepository.existsByShortCode("xyz789");

	        assertThat(exists).isTrue();
	    }

	    @Test
	    void shouldReturnEmptyWhenShortCodeDoesNotExist() {

	        Optional<UrlMapping> result =
	                urlMappingRepository.findByShortCode("missing");

	        assertThat(result).isEmpty();
	    }
}
