package com.github.deepthi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.github.deepthi.dto.DailyClickStatsResponse;
import com.github.deepthi.entity.UrlMapping;
import com.github.deepthi.repository.UrlClickRepository;

@SpringBootTest
public class UrlClickServiceTest {

	  @MockitoBean
	    private UrlClickRepository urlClickRepository;

	    @Autowired
	    private UrlClickService urlClickService;

	    @Test
	    void recordClickShouldSaveClickRecord() {
	        UrlMapping mapping = mock(UrlMapping.class);

	        urlClickService.recordClick(mapping);

	        verify(urlClickRepository).save(
	                org.mockito.ArgumentMatchers.argThat(
	                        click -> click.getUrlMapping() == mapping
	                )
	        );
	    }


@Test
void getTotalClicksShouldReturnRepositoryCount() {
    String shortCode = "click001";

    org.mockito.Mockito
        .when(urlClickRepository.countByUrlMapping_ShortCode(shortCode))
        .thenReturn(5L);

    long totalClicks = urlClickService.getTotalClicks(shortCode);

    org.assertj.core.api.Assertions
        .assertThat(totalClicks)
        .isEqualTo(5L);

    verify(urlClickRepository)
        .countByUrlMapping_ShortCode(shortCode);
}


@Test
void getDailyClicksShouldReturnDailyStatistics() {
    // Arrange
    String shortCode = "daily001";
    LocalDate today = LocalDate.now();

    UrlClickRepository.DailyClickCount dailyCount =
            new UrlClickRepository.DailyClickCount() {
        @Override
        public LocalDate getClickDate() {
            return today;
        }

        @Override
        public Long getClicks() {
            return 3L;
        }
    };

    when(urlClickRepository.findDailyClicksByShortCode(shortCode))
            .thenReturn(List.of(dailyCount));

    // Act
    List<DailyClickStatsResponse> result =
            urlClickService.getDailyClicks(shortCode);

    // Assert
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getDate()).isEqualTo(today);
    assertThat(result.get(0).getClicks()).isEqualTo(3L);

    verify(urlClickRepository).findDailyClicksByShortCode(shortCode);
}


}
