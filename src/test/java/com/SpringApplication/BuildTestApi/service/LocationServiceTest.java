package com.SpringApplication.BuildTestApi.service;

import com.spring.buildapi.model.IpLocationDetails;
import com.spring.buildapi.service.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @InjectMocks
    private LocationService locationService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    private static final String VALID_IP = "192.168.0.1";
    private static final String CITY = "TestCity";
    private static final String COUNTRY = "TestCountry";
    private static final String COUNTRY_CODE = "TC";

    private IpLocationDetails locationDetails;

    @BeforeEach
    void setUp() {
        locationDetails = new IpLocationDetails();
        locationDetails.setCity(CITY);
        locationDetails.setCountry(COUNTRY);
        locationDetails.setCountryCode(COUNTRY_CODE);
    }

    @Test
    void testGetLocationDetailsByIp_Success() {
        when(restTemplate.getForObject(anyString(), eq(IpLocationDetails.class)))
                .thenReturn(locationDetails);
        IpLocationDetails result = locationService.getLocationDetailsByIp(VALID_IP);
        assertNotNull(result);
        assertEquals(CITY, result.getCity());
        assertEquals(COUNTRY, result.getCountry());
        assertEquals(COUNTRY_CODE, result.getCountryCode());

        verify(restTemplate, times(1)).getForObject(anyString(), eq(IpLocationDetails.class));
    }


    @Test
    void testGetLocationDetailsByIp_NullResponse() {
        when(restTemplate.getForObject(anyString(), eq(IpLocationDetails.class)))
                .thenReturn(null);
        IpLocationDetails result = locationService.getLocationDetailsByIp(VALID_IP);

        assertNull(result);
        verify(restTemplate, times(1)).getForObject(anyString(), eq(IpLocationDetails.class));
    }
}
