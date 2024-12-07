package com.spring.buildapi.controller;

import com.spring.buildapi.model.IpWeatherResponse;
import com.spring.buildapi.model.IpLocationDetails;
import com.spring.buildapi.model.RequestInfo;
import com.spring.buildapi.model.WeatherApiResponse;
import com.spring.buildapi.exceptions.RateLimitExceededException;
import com.spring.buildapi.exceptions.ApiException;
import com.spring.buildapi.service.LocationService;
import com.spring.buildapi.service.WeatherService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.MeterRegistry;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);
    private static final Logger sensitiveLogger = LoggerFactory.getLogger("sensitiveLogger");

    private final LocationService locationService;
    private final WeatherService weatherService;

    // Metrics
//    private final Counter requestCount;
//    private final Timer responseTimeTimer;

    // IP request counts and timestamps
    private final Map<String, RequestInfo> requestMap = new HashMap<>();
    private final long TIME_WINDOW_MS;
    private final int MAX_REQUESTS_PER_MINUTE;

    // regex pattern to check ip address
    private static final Pattern IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

    @Autowired
    public ApiController(LocationService locationService, WeatherService weatherService,
                         @Value("${timeWindow}") long timeWindow,
                         @Value("${reqLimit}") int reqLimit) {
        this.locationService = locationService;
        this.weatherService = weatherService;
        this.TIME_WINDOW_MS = timeWindow;
        this.MAX_REQUESTS_PER_MINUTE = reqLimit;

//        // Initialize custom metrics
//        this.requestCount = meterRegistry.counter("api_requests_total");
//        this.responseTimeTimer = meterRegistry.timer("api_requests_timer");
    }

    @GetMapping("/weather-by-ip")
    public IpWeatherResponse getWeatherByIp(@RequestParam(value = "ip", required = false) String ip) {
        logger.info("Received weather request");

        long startTime = System.currentTimeMillis();  // Start timing the request

        if (ip == null || ip.isEmpty()) {
            ip = getClientIp();
        }

        // Validate and sanitize IP
        if (!isValidIp(ip)) {
            logger.error("Invalid IP format: {}", ip);
            throw new ApiException("Invalid IP address format.");
        }

        // Check rate limit
        if (isRateLimited(ip)) {
            sensitiveLogger.warn("Rate limit exceeded for IP: {}", ip);
            throw new RateLimitExceededException("Rate limit exceeded. Please try again later.");
        }

        try {
            // Fetch location by IP
            IpLocationDetails locationDetails = locationService.getLocationDetailsByIp(ip);
            // Fetch weather data
            WeatherApiResponse weatherData = weatherService.getWeatherDataByCityName(locationDetails.getCity(), locationDetails.getCountryCode());

            // Build response
            IpWeatherResponse response = buildWeatherResponse(ip, locationDetails, weatherData);

//            // Record the request and response time metrics
//            requestCount.increment();  // Increment the request count metric
//            responseTimeTimer.record(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS); // Record response time

            return response;

        } catch (Exception e) {
            sensitiveLogger.error("Failed to fetch weather data for IP: {}. Error: {}", ip, e.getMessage(), e);
            throw new ApiException("Failed to fetch weather data. Please try again later.");
        }
    }

    private boolean isValidIp(String ip) {
        return IP_PATTERN.matcher(ip).matches();
    }

    private boolean isRateLimited(String ip) {
        long currentTime = System.currentTimeMillis();

        // Check if IP exists in map
        RequestInfo requestInfo = requestMap.getOrDefault(ip, new RequestInfo(0, currentTime));

        // If within the time window
        if (currentTime - requestInfo.getTimeStamp() < TIME_WINDOW_MS) {
            if (requestInfo.getRequestCount() < MAX_REQUESTS_PER_MINUTE) {
                requestInfo.incrementRequestCount();
                requestMap.put(ip, requestInfo);
                return false; // Not rate-limited
            } else {
                return true; // Rate limit exceeded
            }
        } else {
            requestMap.put(ip, new RequestInfo(1, currentTime));
            return false; // Not rate-limited
        }
    }

    private IpWeatherResponse buildWeatherResponse(String ip, IpLocationDetails locationDetails, WeatherApiResponse weatherData) {
        IpWeatherResponse response = new IpWeatherResponse();
        response.setIp(ip);

        // Set location information
        IpWeatherResponse.Location location = new IpWeatherResponse.Location();
        location.setCity(locationDetails.getCity());
        location.setCountry(locationDetails.getCountry());
        response.setLocation(location);

        // Set weather information
        IpWeatherResponse.Weather weather = new IpWeatherResponse.Weather();
        if (weatherData != null && weatherData.getMain() != null && weatherData.getWeather() != null) {
            double temperatureInCelsius = weatherData.getMain().getTemp() - 273.15;
            BigDecimal formattedTemperature = new BigDecimal(temperatureInCelsius).setScale(1, RoundingMode.HALF_UP);
            weather.setTemperature(formattedTemperature.doubleValue());

            int roundedHumidity = (int) Math.round(weatherData.getMain().getHumidity());
            weather.setHumidity(roundedHumidity);
            weather.setDescription(weatherData.getWeather().get(0).getDescription());
        }
        response.setWeather(weather);

        logger.info("Successfully fetched weather data for IP.");
        return response;
    }

    private String getClientIp() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null) {
            clientIp = request.getRemoteAddr();
        }
        sensitiveLogger.debug("Request received from IP: {}", clientIp);
        return clientIp;
    }
}
