package com.github.deepthi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import com.github.deepthi.dto.CachedUrlMapping;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, CachedUrlMapping> urlMappingRedisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, CachedUrlMapping> template = new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        JacksonJsonRedisSerializer<CachedUrlMapping> jsonSerializer =
                new JacksonJsonRedisSerializer<>(CachedUrlMapping.class);

        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        return template;
    }
}
