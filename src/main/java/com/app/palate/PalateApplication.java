package com.app.palate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = "com.app.palate")
public class PalateApplication {

    public static void main(String[] args) {
        SpringApplication.run(PalateApplication.class, args);
    }
}