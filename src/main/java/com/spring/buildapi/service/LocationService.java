package com.spring.buildapi.service;

import com.spring.buildapi.constants.ApiConstants;
import com.spring.buildapi.model.IpLocationDetails;
import org.springframework.cache.annotation.Cacheable; // Import the Cacheable annotation
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LocationService {

    private final RestTemplate restTemplate;

    public LocationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 5)) // Retry logic for transient errors
    @Cacheable(value = "locationCache", key = "#ip", unless = "#result == null") // Caching logic
    public IpLocationDetails getLocationDetailsByIp(String ip) {
        String url = ApiConstants.IP_API_URL + ip;
        return restTemplate.getForObject(url, IpLocationDetails.class);
    }
}
