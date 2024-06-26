package com.yupi.springbootinit.config;

import lombok.Data;
import org.apache.xmlbeans.impl.xb.xmlconfig.ConfigDocument;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 黄昊
 * @version 1.0
 **/
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissionConfig {
    private Integer database;
    private String host;
    private String port;

    //    private String password;
    @Bean
    public RedissonClient getRedissionClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                .setDatabase(1);
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
