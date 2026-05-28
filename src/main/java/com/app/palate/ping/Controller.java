package com.app.palate.ping;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/palate/ping")
public class Controller {

    @GetMapping
    public String ping(){

        System.out.println("Backend Running");

        return "pong";
    }
}