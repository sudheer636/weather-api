package com.SpringApplication.BuildTestApi.service;

import com.spring.buildapi.service.CacheExpirationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheExpirationServiceTest {

    @InjectMocks
    private CacheExpirationService cacheExpirationService;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache locationCache;

    @Mock
    private Cache weatherCache;

    @Test
    void testClearCache_NoCacheFound() {
        when(cacheManager.getCache("nonExistentCache")).thenReturn(null);
        cacheExpirationService.clearCache("nonExistentCache");

        verify(cacheManager, times(1)).getCache("nonExistentCache");
    }

}
