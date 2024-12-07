package com.SpringApplication.BuildTestApi.controller;

import com.spring.buildapi.BuildTestApiApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = BuildTestApiApplication.class)
@AutoConfigureMockMvc
public class ApiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetWeatherByIp_Success() throws Exception {
        String validIp = "8.8.8.8";

        mockMvc.perform(get("/api/weather-by-ip").param("ip", validIp))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ip").value(validIp))
                .andExpect(jsonPath("$.location.city").exists())
                .andExpect(jsonPath("$.location.country").exists())
                .andExpect(jsonPath("$.weather.temperature").exists())
                .andExpect(jsonPath("$.weather.humidity").exists())
                .andExpect(jsonPath("$.weather.description").exists());
    }


    @Test
    void testGetWeatherByIp_RateLimitExceeded() throws Exception {
        String ip = "192.168.1.1";

        // Simulate 6 requests to trigger rate limit (assuming the limit is 5 requests)

        mockMvc.perform(get("/api/weather-by-ip").param("ip", ip))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/weather-by-ip").param("ip", ip))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/weather-by-ip").param("ip", ip))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/weather-by-ip").param("ip", ip))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/weather-by-ip").param("ip", ip))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/weather-by-ip").param("ip", ip))
                .andExpect(status().isTooManyRequests()); // Expect 429 Too Many Requests
    }

    @Test
    void testGetWeatherByIp_MissingIp() throws Exception {
        mockMvc.perform(get("/api/weather-by-ip"))
                .andExpect(status().isOk()) // Expect 200 OK
                .andExpect(jsonPath("$.ip").exists())
                .andExpect(jsonPath("$.location.city").exists())
                .andExpect(jsonPath("$.weather.temperature").exists());
    }
}
