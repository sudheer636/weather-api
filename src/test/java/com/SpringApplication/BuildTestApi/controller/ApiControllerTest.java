package com.SpringApplication.BuildTestApi.controller;

import com.spring.buildapi.controller.ApiController;
import com.spring.buildapi.model.IpWeatherResponse;
import com.spring.buildapi.model.IpLocationDetails;
import com.spring.buildapi.model.WeatherApiResponse;
import com.spring.buildapi.exceptions.RateLimitExceededException;
import com.spring.buildapi.exceptions.ApiException;
import com.spring.buildapi.service.LocationService;
import com.spring.buildapi.service.WeatherService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApiControllerTest {

    @Mock
    private LocationService locationService;

    @Mock
    private WeatherService weatherService;

    @Mock
    private HttpServletRequest request;

    private ApiController apiController;
    private static final String VALID_IP = "192.168.0.1";
    private static final String INVALID_IP = "999.999.999.999";
    private static final String CITY = "Test City";
    private static final String COUNTRY = "US";
    private static final String COUNTRY_CODE = "US";

    @BeforeEach
    void setUp() {
        apiController = new ApiController(locationService, weatherService, 60000L, 1);
    }


    @Test
    void testGetWeatherByIp_Success() {
        // Arrange
        IpLocationDetails locationDetails = new IpLocationDetails();
        locationDetails.setCity("Test City");
        locationDetails.setCountry("US");
        locationDetails.setCountryCode("US");

        WeatherApiResponse weatherApiResponse = new WeatherApiResponse();
        WeatherApiResponse.Main main = new WeatherApiResponse.Main();
        main.setTemp(300.0);
        main.setHumidity(60.0);

        WeatherApiResponse.Weather weather = new WeatherApiResponse.Weather();
        weather.setDescription("Clear sky");

        weatherApiResponse.setMain(main);
        weatherApiResponse.setWeather(List.of(weather));

        when(locationService.getLocationDetailsByIp("192.168.0.1")).thenReturn(locationDetails);
        when(weatherService.getWeatherDataByCityName("Test City", "US")).thenReturn(weatherApiResponse);


        IpWeatherResponse response = apiController.getWeatherByIp("192.168.0.1");

        assertNotNull(response);
        assertEquals("Test City", response.getLocation().getCity());
        assertEquals("US", response.getLocation().getCountry());
        assertEquals(26.9, response.getWeather().getTemperature(), 0.1);
        assertEquals(60, response.getWeather().getHumidity());
        assertEquals("Clear sky", response.getWeather().getDescription());

        verify(locationService).getLocationDetailsByIp("192.168.0.1");
        verify(weatherService).getWeatherDataByCityName("Test City", "US");
    }

    @Test
    void testGetWeatherByIp_InvalidIpFormat() {
        ApiException exception = assertThrows(ApiException.class, () -> {
            apiController.getWeatherByIp(INVALID_IP);
        });
        assertEquals("Invalid IP address format.", exception.getMessage());
    }

    @Test
    void testGetWeatherByIp_RateLimitExceeded() {
        IpLocationDetails locationDetails = new IpLocationDetails();
        locationDetails.setCity(CITY);
        locationDetails.setCountry(COUNTRY);
        locationDetails.setCountryCode(COUNTRY_CODE);

        WeatherApiResponse weatherApiResponse = new WeatherApiResponse();
        WeatherApiResponse.Main main = new WeatherApiResponse.Main();
        main.setTemp(300.0);
        main.setHumidity(60.0);

        WeatherApiResponse.Weather weather = new WeatherApiResponse.Weather();
        weather.setDescription("Clear sky");

        weatherApiResponse.setMain(main);
        weatherApiResponse.setWeather(List.of(weather));

        when(locationService.getLocationDetailsByIp(VALID_IP)).thenReturn(locationDetails);
        when(weatherService.getWeatherDataByCityName(CITY, COUNTRY_CODE)).thenReturn(weatherApiResponse);

        apiController.getWeatherByIp(VALID_IP);

        RateLimitExceededException exception = assertThrows(RateLimitExceededException.class, () -> {
            apiController.getWeatherByIp(VALID_IP);
        });

        assertEquals("Rate limit exceeded. Please try again later.", exception.getMessage());

        verify(locationService, times(1)).getLocationDetailsByIp(VALID_IP);
        verify(weatherService, times(1)).getWeatherDataByCityName(CITY, COUNTRY_CODE);
    }

    @Test
    void testGetWeatherByIp_FailedToFetchWeatherData() {
        IpLocationDetails locationDetails = new IpLocationDetails();
        locationDetails.setCity(CITY);
        locationDetails.setCountry(COUNTRY);
        locationDetails.setCountryCode(COUNTRY_CODE);

        when(locationService.getLocationDetailsByIp(VALID_IP)).thenReturn(locationDetails);
        when(weatherService.getWeatherDataByCityName(CITY, COUNTRY_CODE)).thenThrow(new RuntimeException("Service unavailable"));

        ApiException exception = assertThrows(ApiException.class, () -> {
            apiController.getWeatherByIp(VALID_IP);
        });
        assertEquals("Failed to fetch weather data. Please try again later.", exception.getMessage());
    }

    @Test
    void testGetWeatherByIp_MissingIpParam() {

        request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.0.1");

        ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(requestAttributes);
        IpLocationDetails locationDetails = new IpLocationDetails();
        locationDetails.setCity(CITY);
        locationDetails.setCountry(COUNTRY);
        locationDetails.setCountryCode(COUNTRY_CODE);

        WeatherApiResponse weatherApiResponse = new WeatherApiResponse();
        WeatherApiResponse.Main main = new WeatherApiResponse.Main();
        main.setTemp(300.0);
        main.setHumidity(60.0);

        WeatherApiResponse.Weather weather = new WeatherApiResponse.Weather();
        weather.setDescription("Clear sky");

        weatherApiResponse.setMain(main);
        weatherApiResponse.setWeather(List.of(weather));

        when(locationService.getLocationDetailsByIp(anyString())).thenReturn(locationDetails);
        when(weatherService.getWeatherDataByCityName(CITY, COUNTRY_CODE)).thenReturn(weatherApiResponse);

        IpWeatherResponse response = apiController.getWeatherByIp(null);

        assertNotNull(response);
        assertEquals(CITY, response.getLocation().getCity());
    }
}
