package com.example.demo_database.redisRepository;

import com.example.demo_database.dto.JwtObject.RedisToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedisTokenRepository extends JpaRepository<RedisToken, String> {

}
