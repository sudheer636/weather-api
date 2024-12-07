package com.spring.buildapi.service;

import com.spring.buildapi.constants.ApiConstants;
import com.spring.buildapi.model.WeatherApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable; // Import Cacheable annotation
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {

    @Value("${openWeatherApiKey}")
    private String openWeatherApiKey;

    private final RestTemplate restTemplate;

    public WeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 5)) // Retry logic for transient errors
    @Cacheable(value = "weatherCache", key = "#city + ',' + #countryCode", unless = "#result == null") // Caching logic
    public WeatherApiResponse getWeatherDataByCityName(String city, String countryCode) {
        String url = ApiConstants.WEATHER_API_URL + city + "," + countryCode + "&appid=" + openWeatherApiKey;
        return restTemplate.getForObject(url, WeatherApiResponse.class);
    }
}
