package com.github.deepthi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.deepthi.dto.CachedUrlMapping;

@SpringBootTest
public class RedisConfigTest {

	   @Autowired
	    private RedisTemplate<String, CachedUrlMapping> urlMappingRedisTemplate;

	    @Test
	    void shouldStoreAndRetrieveCachedUrlMapping() {
	        String key = "test:url:redis-connectivity";

	        CachedUrlMapping original = new CachedUrlMapping(
	                1L,
	                "test1234",
	                "https://example.com",
	                null, null
	        );

	        try {
	            urlMappingRedisTemplate.opsForValue().set(key, original);

	            CachedUrlMapping retrieved =
	                    urlMappingRedisTemplate.opsForValue().get(key);

	            assertNotNull(retrieved);
	            assertEquals("test1234", retrieved.getShortCode());
	            assertEquals("https://example.com", retrieved.getOriginalUrl());
	        } finally {
	            urlMappingRedisTemplate.delete(key);
	        }
	    }
}
