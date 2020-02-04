package com.atguigu.gmall.locks.config;


import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// 配置参考MAVEN的Redisson的wiki

@Configuration
public class RedissonConfig {

    @Bean
    RedissonClient redisson(RedisProperties properties) {
        Config config = new Config();
        // 单节点
        config.useSingleServer().setAddress("redis://"+properties.getHost() + ":" + properties.getPort());
        // 多节点分布式锁
        // config.useClusterServers().addNodeAddress("redis://"+properties.getHost() + ":" + properties.getPort());
        return Redisson.create(config);
    }
}
