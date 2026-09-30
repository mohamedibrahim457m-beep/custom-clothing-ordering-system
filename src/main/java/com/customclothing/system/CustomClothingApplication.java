package com.customclothing.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CustomClothingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomClothingApplication.class, args);
    }

}
