package com.g_wuy.swp391.voltera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VolteraWebAppDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(VolteraWebAppDemoApplication.class, args);
    }

}
