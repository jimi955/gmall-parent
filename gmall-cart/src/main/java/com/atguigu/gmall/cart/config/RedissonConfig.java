package com.atguigu.gmall.cart.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class RedissonConfig {

    @Bean
    RedissonClient redisson(RedisProperties properties) throws IOException {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://"+properties.getHost() + ":" + properties.getPort());
        return Redisson.create(config);
    }
}
