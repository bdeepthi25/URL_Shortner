package com.github.deepthi.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.deepthi.entity.UrlClick;

public interface UrlClickRepository  extends JpaRepository<UrlClick, Long> {

	  long countByUrlMapping_ShortCode(String shortCode);

	  @Query(value = """
	            SELECT CAST(uc.clicked_at AS DATE) AS clickDate,
	                   COUNT(*) AS clicks
	            FROM url_click uc
	            JOIN url_mapping um
	                ON uc.url_mapping_id = um.id
	            WHERE um.short_code = :shortCode
	            GROUP BY CAST(uc.clicked_at AS DATE)
	            ORDER BY CAST(uc.clicked_at AS DATE)
	            """, nativeQuery = true)
	    List<DailyClickCount> findDailyClicksByShortCode(
	            @Param("shortCode") String shortCode);

	    interface DailyClickCount {
	        LocalDate getClickDate();
	        Long getClicks();
	    }


	    void deleteByUrlMapping_ShortCode(String shortCode);

	    @Modifying
	    @Query("""
	        DELETE FROM UrlClick c
	        WHERE c.urlMapping.id IN (
	            SELECT u.id
	            FROM UrlMapping u
	            WHERE u.expiresAt IS NOT NULL
	              AND u.expiresAt <= :cutoff
	        )
	        """)
	    int deleteClicksForExpiredUrlsBefore(
	            @Param("cutoff") LocalDateTime cutoff);
}
