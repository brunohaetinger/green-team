package com.greenteam.webserverdatacollector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ReportApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReportApiApplication.class, args);
    }
}
