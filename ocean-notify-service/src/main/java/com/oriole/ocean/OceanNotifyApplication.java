package com.oriole.ocean;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class OceanNotifyApplication {
    public static void main(String[] args) {
        SpringApplication.run(OceanNotifyApplication.class, args);
    }
}