package com.github.deepthi.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
public class UrlMappingTest 
{
	    @Test
	    void constructorShouldInitializeUrlFields() 
	    {

	        UrlMapping mapping = new UrlMapping(
	                "aB12xY",
	                "https://example.com/products"
	        );

	        assertEquals("aB12xY", mapping.getShortCode());
	        assertEquals(
	                "https://example.com/products",
	                mapping.getOriginalUrl()
	        );

	        assertNull(mapping.getId());
	        assertNull(mapping.getCreatedAt());
	    }
}
