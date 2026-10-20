package com.github.deepthi.repository;

import java.time.LocalDateTime;
import java.util.List;
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
	    

		@Test
		void shouldDeleteOnlyUrlsExpiredBeforeOrAtCutoff() {
		
		    LocalDateTime cutoff =
		            LocalDateTime.of(2026, 10, 1, 0, 0);
		
		    UrlMapping expiredMapping = new UrlMapping(
		            "cleanup01",
		            "https://example.com/expired"
		    );
		    expiredMapping.setExpiresAt(cutoff.minusDays(1));
		
		    UrlMapping cutoffMapping = new UrlMapping(
		            "cleanup02",
		            "https://example.com/cutoff"
		    );
		    cutoffMapping.setExpiresAt(cutoff);
		
		    UrlMapping activeMapping = new UrlMapping(
		            "cleanup03",
		            "https://example.com/active"
		    );
		    activeMapping.setExpiresAt(cutoff.plusDays(1));
		
		    UrlMapping noExpiryMapping = new UrlMapping(
		            "cleanup04",
		            "https://example.com/no-expiry"
		    );
		
		    urlMappingRepository.saveAll(List.of(
		            expiredMapping,
		            cutoffMapping,
		            activeMapping,
		            noExpiryMapping
		    ));
		
		    int deletedCount =
		            urlMappingRepository.deleteExpiredUrlsBefore(cutoff);
		
		    assertThat(deletedCount).isEqualTo(2);
		
		    assertThat(urlMappingRepository.findByShortCode("cleanup01"))
		            .isEmpty();
		
		    assertThat(urlMappingRepository.findByShortCode("cleanup02"))
		            .isEmpty();
		
		    assertThat(urlMappingRepository.findByShortCode("cleanup03"))
		            .isPresent();
		
		    assertThat(urlMappingRepository.findByShortCode("cleanup04"))
		            .isPresent();
		}

}
