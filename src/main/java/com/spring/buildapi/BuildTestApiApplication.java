package com.spring.buildapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class BuildTestApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BuildTestApiApplication.class, args);
    }

}
