package com.urlShortener.UrlShortenerProject.config;
import com.urlShortener.UrlShortenerProject.entity.UrlMapping;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, UrlMapping> redisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, UrlMapping> template =
                new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer keySerializer =
                new StringRedisSerializer();

        JacksonJsonRedisSerializer<UrlMapping> valueSerializer =
                new JacksonJsonRedisSerializer<>(UrlMapping.class);

        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);

        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);

        template.afterPropertiesSet();

        return template;
    }
}