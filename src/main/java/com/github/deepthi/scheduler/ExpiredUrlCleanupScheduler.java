package com.github.deepthi.scheduler;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.github.deepthi.service.UrlMappingService;

@Component
public class ExpiredUrlCleanupScheduler {
	 private static final Logger logger =
	            LoggerFactory.getLogger(ExpiredUrlCleanupScheduler.class);

	    private final UrlMappingService urlMappingService;

	    public ExpiredUrlCleanupScheduler(
	            UrlMappingService urlMappingService) 
	    
	    {
	        this.urlMappingService = urlMappingService;
	    }

	    @Scheduled(cron = "${url.cleanup.cron}")
	    public void cleanupExpiredUrls() 
	    {
	        int deletedCount = urlMappingService.cleanupExpiredUrls();

	        logger.info("Expired URL cleanup completed. Deleted {} records.");
	    }
}
