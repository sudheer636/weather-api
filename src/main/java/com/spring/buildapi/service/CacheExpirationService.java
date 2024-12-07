package com.spring.buildapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling  // Enable scheduling of tasks
public class CacheExpirationService {

    private final CacheManager cacheManager;

    public CacheExpirationService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    // Scheduled task to clear the location and weather caches every 10 minutes
    @Scheduled(fixedRate = 600000)
    public void clearCache() {
        // Clear location and weather caches
        clearCache("locationCache");
        clearCache("weatherCache");
    }

    public void clearCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}

