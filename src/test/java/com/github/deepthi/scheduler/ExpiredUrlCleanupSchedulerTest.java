package com.github.deepthi.scheduler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.deepthi.service.UrlMappingService;

@ExtendWith(MockitoExtension.class)
public class ExpiredUrlCleanupSchedulerTest {
    @Mock
    private UrlMappingService urlMappingService;

    @Test
    void cleanupExpiredUrlsShouldInvokeService() {

        when(urlMappingService.cleanupExpiredUrls())
                .thenReturn(3);

        ExpiredUrlCleanupScheduler scheduler =
                new ExpiredUrlCleanupScheduler(urlMappingService);

        assertDoesNotThrow(scheduler::cleanupExpiredUrls);

        verify(urlMappingService).cleanupExpiredUrls();
        verifyNoMoreInteractions(urlMappingService);
    }
}
