package com.example.inspector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class InspectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(InspectorApplication.class, args);
    }
}
