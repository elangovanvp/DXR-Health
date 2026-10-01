package com.dxr.health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DxrHealthApplication {
    public static void main(String[] args) {
        SpringApplication.run(DxrHealthApplication.class, args);
    }
}
