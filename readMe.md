# Spring Boot API - Weather by IP Address

## Overview
This Spring Boot application provides an API that retrieves weather information based on the user's IP address. The application performs the following steps:

- Fetches the location information (City and Country) based on the provided or client’s IP address.
- Retrieves the weather data for that location (city and country) from the OpenWeather API.
- Returns a consolidated response containing the IP, location details, and weather data (temperature, humidity, description).

The application includes caching, rate-limiting, error handling, and retry logic to ensure reliability and performance.

## Features
- **Rate Limiting**: To prevent abuse, the API limits the number of requests from the same IP address within a specified time window.
- **Caching**: Caching is used for both location and weather data to avoid redundant API calls and improve performance.
- **Retry Logic**: For transient errors (such as network issues), the system automatically retries failed requests.
- **Cache Expiration**: Cache entries are cleared every 10 minutes to keep data up-to-date.
- **Error Handling**: The application handles common error scenarios, such as invalid IP format, rate limit exceeded, and failed API calls.

## API Endpoints

### 1. `GET /api/weather-by-ip`
**Description**: Fetch weather information based on the provided or detected IP address.

#### Request Parameter:
- **ip (optional)**: The IP address to fetch weather information for. If not provided, the client's IP will be used.

#### Response:
```json
{
  "ip": "192.168.1.1",
  "location": {
    "city": "New York",
    "country": "United States"
  },
  "weather": {
    "temperature": 20.5,
    "humidity": 60,
    "description": "Clear sky"
  }
}
```

## Possible Responses:
- **200 OK**: Successfully fetched weather data.
- **400 Bad Request**: Invalid IP address format.
- **429 Too Many Requests**: Rate limit exceeded for the IP address.
- **500 Internal Server Error**: Failed to retrieve weather data.

## Rate Limiting
Rate Limiting is applied to each unique IP address. The rate limit is defined by two parameters:
- **timeWindow**: The time window (in milliseconds) within which the rate limit is applied.
- **reqLimit**: The maximum number of allowed requests per IP address per time window.

If an IP exceeds the rate limit, a `429 Too Many Requests` response is returned.

## Caching
- **Location Cache**: Caches the IP-to-location (city, country) information.
- **Weather Cache**: Caches the weather information based on city and country.

Both caches expire every 10 minutes, as defined by the scheduled task in `CacheExpirationService`.

## Cache Expiration
A scheduled task runs every 10 minutes and clears the `locationCache` and `weatherCache`.

## Error Handling
The application handles the following errors:

- **Invalid IP Format**: If the provided IP address does not match the required format, an `ApiException` is thrown with the message `"Invalid IP address format."`
- **Rate Limit Exceeded**: If an IP exceeds the maximum allowed requests per minute within the time window, a `RateLimitExceededException` is thrown with the message `"Rate limit exceeded. Please try again later."`
- **API Failures**: If either the location or weather API fails to return data, an `ApiException` is thrown with the message `"Failed to fetch weather data. Please try again later."`

## Retry Logic
For transient errors while fetching location or weather data (e.g., network issues), the system will automatically retry up to 5 times with a delay of 5 milliseconds between attempts.

## Dependencies
The application requires the following dependencies:
- **Spring Boot** (for building the application)
- **Spring Web** (for RESTful APIs)
- **Spring Cache** (for caching)
- **Spring Retry** (for retry logic)
- **Micrometer** (for metrics and monitoring, though not fully enabled in this code)
- **RestTemplate** (for HTTP requests to external APIs)

## External API URLs
- **IP Location API**: `http://ip-api.com/json/?IP_ADDRESS=`
- **Weather API**: `https://api.openweathermap.org/data/2.5/weather?q=`

## Configuration
The following configuration properties can be customized via the `application.properties` file:

- **timeWindow**: The time window in milliseconds (default: `60000ms = 1 minute`).
- **reqLimit**: The maximum number of requests per IP address per minute (default: `60 requests`).
- **openWeatherApiKey**: Your OpenWeather API key (required for weather data fetching).

## Project Structure

```yaml
src/main/java/com/spring/buildapi
├── controller: Contains the main controller (`ApiController`) that handles incoming API requests.
├── config: Configuration classes, including cache configuration and RestTemplate setup.
├── constants: API constants for external URLs.
├── exceptions: Custom exception classes for API errors and rate limiting.
├── model: Data model classes for API requests and responses.
└── service: Service layer for fetching location and weather data and managing cache expiration.

## Possible Scenarios Handled

1. **Valid IP Address and Within Rate Limit**:
   - The user provides a valid IP address, and the request is within the rate limit.
   - The system returns the weather information for the location of the IP.

2. **Invalid IP Address**:
   - The user provides an invalid IP address (e.g., `123.456.789.0`).
   - The system responds with a `400 Bad Request` error: `"Invalid IP address format."`

3. **Rate Limit Exceeded**:
   - The same IP makes more requests than the allowed limit within the time window.
   - The system responds with a `429 Too Many Requests` error: `"Rate limit exceeded. Please try again later."`

4. **External API Failure**:
   - The external APIs (IP Location or Weather API) fail due to network issues or invalid responses.
   - The system retries up to 5 times and, if unsuccessful, throws a `500 Internal Server Error`.

5. **Successful API Response**:
   - The system fetches the location and weather data successfully and returns the weather information along with the location details in the response.

6. **Cache Hit and Miss**:
   - The system caches the location and weather data for a specified time.
   - When the same IP or city is requested again within the cache lifetime, the system returns cached data (`cache hit`).
   - After cache expiration (every 10 minutes), the data is fetched again (`cache miss`).
