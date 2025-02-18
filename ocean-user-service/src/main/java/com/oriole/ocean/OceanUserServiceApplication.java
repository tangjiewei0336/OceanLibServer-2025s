package com.oriole.ocean;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class OceanUserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OceanUserServiceApplication.class, args);
    }
}