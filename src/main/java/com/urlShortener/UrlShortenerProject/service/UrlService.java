package com.urlShortener.UrlShortenerProject.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.urlShortener.UrlShortenerProject.DTO.CreateUrlRequest;
import com.urlShortener.UrlShortenerProject.DTO.UrlResponse;
import com.urlShortener.UrlShortenerProject.entity.UrlMapping;
import com.urlShortener.UrlShortenerProject.exception.UrlExpiredException;
import com.urlShortener.UrlShortenerProject.exception.UrlNotFoundException;
import com.urlShortener.UrlShortenerProject.repository.UrlRepository;

@Service
public class UrlService {

    private final UrlRepository urlRepository;
    private final UrlCacheService urlCacheService;
    private final String baseUrl;

    public UrlService(
            UrlRepository urlRepository,
            UrlCacheService urlCacheService,
            @Value("${app.base-url}") String baseUrl) {

        this.urlRepository = urlRepository;
        this.urlCacheService = urlCacheService;
        this.baseUrl = baseUrl;
    }

    public UrlResponse createShortUrl(CreateUrlRequest request) {

        String shortCode = generateUniqueShortCode();

        UrlMapping urlMapping = UrlMapping.builder()
                .originalUrl(request.originalUrl())
                .shortCode(shortCode)
                .createdAt(LocalDateTime.now())
                .clickCount(0L)
                .build();

        UrlMapping savedUrl = urlRepository.save(urlMapping);

        // Save to Redis cache
        urlCacheService.save(savedUrl);

        return UrlResponse.from(savedUrl, baseUrl);
    }

    @Transactional
    public UrlMapping getUrlForRedirect(String shortCode) {

        // 1. Check Redis
        UrlMapping urlMapping = urlCacheService.get(shortCode);

        // 2. Cache MISS → PostgreSQL
        if (urlMapping == null) {

            System.out.println("REDIS CACHE MISS");

            urlMapping = urlRepository
                    .findByShortCode(shortCode)
                    .orElseThrow(() ->
                            new UrlNotFoundException(
                                    "Short URL not found: " + shortCode
                            )
                    );

            // Store in Redis
            urlCacheService.save(urlMapping);

        } else {

            System.out.println("REDIS CACHE HIT");
        }

        // 3. Check expiration
        if (urlMapping.getExpiresAt() != null &&
                urlMapping.getExpiresAt()
                        .isBefore(LocalDateTime.now())) {

            throw new UrlExpiredException(
                    "Short URL has expired: " + shortCode
            );
        }

        // 4. Increment click count
        urlMapping.setClickCount(
                urlMapping.getClickCount() + 1
        );

        // 5. Update PostgreSQL
        urlRepository.save(urlMapping);

        // 6. Update Redis
        urlCacheService.save(urlMapping);

        return urlMapping;
    }

    public UrlResponse getUrlDetails(String shortCode) {

        UrlMapping urlMapping = urlRepository
                .findByShortCode(shortCode)
                .orElseThrow(() ->
                        new UrlNotFoundException(
                                "Short URL not found: " + shortCode
                        )
                );

        return UrlResponse.from(urlMapping, baseUrl);
    }

    public void deleteUrl(String shortCode) {

        UrlMapping urlMapping = urlRepository
                .findByShortCode(shortCode)
                .orElseThrow(() ->
                        new UrlNotFoundException(
                                "Short URL not found: " + shortCode
                        )
                );

        urlRepository.delete(urlMapping);

        // Remove from Redis cache
        urlCacheService.delete(shortCode);
    }

    private String generateUniqueShortCode() {

        String shortCode;

        do {
            shortCode = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 6);

        } while (urlRepository.existsByShortCode(shortCode));

        return shortCode;
    }

    public List<UrlResponse> getAllUrls() {

        return urlRepository.findAll()
                .stream()
                .map(urlMapping ->
                        UrlResponse.from(urlMapping, baseUrl)
                )
                .toList();
    }
}