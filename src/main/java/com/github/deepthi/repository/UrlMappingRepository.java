package com.github.deepthi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.deepthi.entity.UrlMapping;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long>
{
	boolean existsByShortCode(String shortCode);
	 java.util.Optional<UrlMapping> findByShortCode(String shortCode);

}
