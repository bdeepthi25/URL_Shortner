package com.github.deepthi.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.deepthi.entity.UrlMapping;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long>
{
	boolean existsByShortCode(String shortCode);
	 java.util.Optional<UrlMapping> findByShortCode(String shortCode);
	 @Modifying
	 @Query("""
	     DELETE FROM UrlMapping u
	     WHERE u.expiresAt IS NOT NULL
	       AND u.expiresAt <= :cutoff
	     """)
	 int deleteExpiredUrlsBefore(
	         @Param("cutoff") LocalDateTime cutoff);
}
