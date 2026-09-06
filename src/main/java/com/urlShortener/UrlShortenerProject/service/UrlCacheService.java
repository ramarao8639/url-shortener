package com.urlShortener.UrlShortenerProject.service;
import com.urlShortener.UrlShortenerProject.entity.UrlMapping;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class UrlCacheService {

    private static final String KEY_PREFIX = "url:";

    private final RedisTemplate<String, UrlMapping> redisTemplate;

    public UrlCacheService(
            RedisTemplate<String, UrlMapping> redisTemplate) {

        this.redisTemplate = redisTemplate;
    }

    public void save(UrlMapping urlMapping) {

        String key = KEY_PREFIX + urlMapping.getShortCode();

        redisTemplate.opsForValue().set(
                key,
                urlMapping,
                Duration.ofHours(1)
        );
    }

    public UrlMapping get(String shortCode) {

        String key = KEY_PREFIX + shortCode;

        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String shortCode) {

        String key = KEY_PREFIX + shortCode;

        redisTemplate.delete(key);
    }
}