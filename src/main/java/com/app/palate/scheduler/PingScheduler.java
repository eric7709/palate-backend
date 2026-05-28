package com.app.palate.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PingScheduler {

    private final RestTemplate restTemplate = new RestTemplate();

    @Scheduled(fixedRate = 10000)
    public void ping() {
        try {
            restTemplate.getForObject(
                "https://palate-backend.onrender.com",
                String.class
            );

        } catch (Exception e) {

            System.out.println("Ping failed");

        }
    }
}