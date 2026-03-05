package com.example.demo_database.redisRepository;

import com.example.demo_database.dto.JwtObject.RedisToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedisTokenRepository extends CrudRepository<RedisToken, String> {

}
