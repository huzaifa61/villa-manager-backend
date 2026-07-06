package com.villamanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VillaManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(VillaManagerApplication.class, args);
    }

}
