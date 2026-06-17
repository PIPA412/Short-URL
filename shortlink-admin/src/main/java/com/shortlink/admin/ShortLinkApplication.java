package com.shortlink.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
@EnableAsync
@SpringBootApplication(scanBasePackages = "com.shortlink")
@EnableConfigurationProperties
public class ShortLinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShortLinkApplication.class, args);
        System.out.println("""

                ==========================================================
                ShortLink Management System started successfully!
                API Documentation: http://localhost:8080/swagger-ui.html
                ==========================================================
                """);
    }
}
