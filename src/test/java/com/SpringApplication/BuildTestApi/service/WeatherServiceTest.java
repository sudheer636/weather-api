package com.SpringApplication.BuildTestApi.service;

import com.spring.buildapi.model.WeatherApiResponse;
import com.spring.buildapi.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@EnableRetry
class WeatherServiceTest {

    @InjectMocks
    private WeatherService weatherService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CacheManager cacheManager;

    private static final String CITY = "TestCity";
    private static final String COUNTRY_CODE = "US";
    private static final String OPEN_WEATHER_API_KEY = "dummyApiKey";

    @BeforeEach
    void setUp() {
        weatherService = new WeatherService(restTemplate);
    }

    @Test
    void testGetWeatherDataByCityName_SuccessfulResponse() {
        WeatherApiResponse weatherApiResponse = new WeatherApiResponse();
        WeatherApiResponse.Main main = new WeatherApiResponse.Main();
        main.setTemp(300.0);
        main.setHumidity(60.0);
        WeatherApiResponse.Weather weather = new WeatherApiResponse.Weather();
        weather.setDescription("Clear sky");
        weatherApiResponse.setMain(main);
        weatherApiResponse.setWeather(List.of(weather));

        when(restTemplate.getForObject(anyString(), eq(WeatherApiResponse.class)))
                .thenReturn(weatherApiResponse);
        WeatherApiResponse response = weatherService.getWeatherDataByCityName(CITY, COUNTRY_CODE);

        assertNotNull(response);
        assertEquals(300.0, response.getMain().getTemp());
        assertEquals("Clear sky", response.getWeather().get(0).getDescription());
    }

    @Test
    void testGetWeatherDataByCityName_CachingBehavior() {
        WeatherApiResponse weatherApiResponse = new WeatherApiResponse();
        WeatherApiResponse.Main main = new WeatherApiResponse.Main();
        main.setTemp(300.0);
        main.setHumidity(60.0);
        WeatherApiResponse.Weather weather = new WeatherApiResponse.Weather();
        weather.setDescription("Clear sky");
        weatherApiResponse.setMain(main);
        weatherApiResponse.setWeather(List.of(weather));

        when(restTemplate.getForObject(anyString(), eq(WeatherApiResponse.class)))
                .thenReturn(weatherApiResponse);

        WeatherApiResponse response1 = weatherService.getWeatherDataByCityName(CITY, COUNTRY_CODE);
        WeatherApiResponse response2 = weatherService.getWeatherDataByCityName(CITY, COUNTRY_CODE);

        assertNotNull(response1);
        assertNotNull(response2);
        assertEquals(response1, response2);
    }
}
