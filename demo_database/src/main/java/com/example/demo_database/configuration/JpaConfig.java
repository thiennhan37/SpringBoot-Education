package com.example.demo_database.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.example.demo_database.repository.jpa"
)
public class JpaConfig {
}
