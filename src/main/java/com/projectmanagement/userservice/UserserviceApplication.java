package com.projectmanagement.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.projectmanagement.userservice.entity")
@EnableJpaRepositories(basePackages = "com.projectmanagement.userservice.repository")
public class UserserviceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserserviceApplication.class, args);
    }
}