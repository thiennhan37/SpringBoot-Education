package com.example.demo_database.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories(
        basePackages = "com.example.demo_database.repository.redis"
)
public class RedisConfig {
}
