package com.urlShortener.UrlShortenerProject.DTO;

import java.time.LocalDateTime;

import com.urlShortener.UrlShortenerProject.entity.UrlMapping;

public record UrlResponse(
        String originalUrl,
        String shortCode,
        String shortUrl,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        Long clickCount
) {

    public static UrlResponse from(
            UrlMapping urlMapping,
            String baseUrl) {

        return new UrlResponse(
                urlMapping.getOriginalUrl(),
                urlMapping.getShortCode(),
                baseUrl + "/" + urlMapping.getShortCode(),
                urlMapping.getCreatedAt(),
                urlMapping.getExpiresAt(),
                urlMapping.getClickCount()
        );
    }
}