package com.g_wuy.swp391.voltera.configuration;

import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    String host;

    @Value("${spring.data.redis.port}")
    int port;

    @Value("${spring.data.redis.password}")
    String password;

    @Value("${spring.data.redis.ssl.enabled:false}")
    boolean sslEnabled;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(host);
        redisConfig.setPort(port);

        if (password != null && !password.isBlank()) {
            redisConfig.setPassword(password);
        }

        LettuceClientConfiguration clientConfig = sslEnabled
                ? LettuceClientConfiguration.builder().useSsl().build()
                : LettuceClientConfiguration.builder().build();

        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
}
