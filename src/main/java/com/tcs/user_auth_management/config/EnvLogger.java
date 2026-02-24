package com.tcs.user_auth_management.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EnvLogger implements CommandLineRunner {

    @Value("${DATABASE_URL:NOT_FOUND}")
    private String dbUrl;

    @Override
    public void run(String... args) {
        System.out.println("---------------------------------");
        System.out.println("LOGGING ENV VAR:");
        System.out.println("DATABASE_URL: " + dbUrl);

        // You can also check the raw System env
        System.out.println("Raw System Env: " + System.getenv("DATABASE_URL"));
        System.out.println("---------------------------------");
    }
}
