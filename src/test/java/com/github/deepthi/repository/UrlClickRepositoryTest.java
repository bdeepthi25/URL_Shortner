package com.github.deepthi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import com.github.deepthi.entity.UrlClick;
import com.github.deepthi.entity.UrlMapping;

import jakarta.persistence.EntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.NONE
)
public class UrlClickRepositoryTest {

    @Autowired
    private UrlClickRepository urlClickRepository;

    @Autowired
    private UrlMappingRepository urlMappingRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldAutomaticallySetAndPersistClickTimestamp() {

        // Arrange: save the URL that this click belongs to
        UrlMapping mapping = new UrlMapping(
            "click001",
            "https://example.com/click-test"
        );

        UrlMapping savedMapping =
            urlMappingRepository.saveAndFlush(mapping);

        // Act: create and save the click without setting clickedAt
        UrlClick click = new UrlClick(savedMapping);

        UrlClick savedClick =
            urlClickRepository.saveAndFlush(click);

        // Assert: verify the ID and timestamp were generated
        assertThat(savedClick.getId()).isNotNull();
        assertThat(savedClick.getClickedAt()).isNotNull();

        // Verify that the timestamp is persisted in the database
        Long clickId = savedClick.getId();

        urlClickRepository.flush();
        entityManager.clear();

        UrlClick retrievedClick =
            urlClickRepository.findById(clickId).orElseThrow();

        assertThat(retrievedClick.getClickedAt())
        .isCloseTo(
            savedClick.getClickedAt(),
            org.assertj.core.api.Assertions.within(
                java.time.Duration.ofNanos(1_000)
            )
        );

        assertThat(retrievedClick.getUrlMapping().getId())
            .isEqualTo(savedMapping.getId());

        assertThat(retrievedClick.getClickedAt())
            .isBeforeOrEqualTo(LocalDateTime.now());
    }


@Test
void shouldCountClicksForShortCode() {
    // Arrange: create and save a URL mapping
    UrlMapping mapping = new UrlMapping(
        "count001",
        "https://example.com/count-test"
    );

    UrlMapping savedMapping =
        urlMappingRepository.saveAndFlush(mapping);

    // Create three clicks for this mapping
    urlClickRepository.saveAndFlush(new UrlClick(savedMapping));
    urlClickRepository.saveAndFlush(new UrlClick(savedMapping));
    urlClickRepository.saveAndFlush(new UrlClick(savedMapping));

    // Act: count clicks for the short code
    long totalClicks =
        urlClickRepository.countByUrlMapping_ShortCode("count001");

    // Assert
    assertThat(totalClicks).isEqualTo(3);
}


@Test
void shouldGroupClicksByDateForShortCode() {
    // Arrange: create and save a URL mapping
    UrlMapping mapping = new UrlMapping(
        "daily001",
        "https://example.com/daily-test"
    );

    UrlMapping savedMapping =
        urlMappingRepository.saveAndFlush(mapping);

    // Create three clicks for the same short code
    urlClickRepository.saveAndFlush(new UrlClick(savedMapping));
    urlClickRepository.saveAndFlush(new UrlClick(savedMapping));
    urlClickRepository.saveAndFlush(new UrlClick(savedMapping));

    // Act: retrieve daily click counts
    List<UrlClickRepository.DailyClickCount> dailyClicks =
        urlClickRepository.findDailyClicksByShortCode("daily001");

    // Assert: all three clicks belong to one date
    assertThat(dailyClicks).hasSize(1);
    assertThat(dailyClicks.get(0).getClickDate())
        .isEqualTo(LocalDate.now());
    assertThat(dailyClicks.get(0).getClicks()).isEqualTo(3L);
}


}
