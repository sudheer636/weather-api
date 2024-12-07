package com.spring.buildapi.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public ConcurrentMapCacheManager cacheManager() {
        // Creating a ConcurrentMapCacheManager with two different caches
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager("locationCache", "weatherCache");
        return cacheManager;
    }
}