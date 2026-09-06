package com.urlShortener.UrlShortenerProject.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.urlShortener.UrlShortenerProject.entity.UrlMapping;

import java.util.Optional;

public interface UrlRepository extends JpaRepository<UrlMapping, Long> {

    Optional<UrlMapping> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    void deleteByShortCode(String shortCode);
}