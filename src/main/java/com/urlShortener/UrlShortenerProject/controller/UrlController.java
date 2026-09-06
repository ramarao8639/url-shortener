package com.urlShortener.UrlShortenerProject.controller;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.urlShortener.UrlShortenerProject.DTO.CreateUrlRequest;
import com.urlShortener.UrlShortenerProject.DTO.UrlResponse;
import com.urlShortener.UrlShortenerProject.entity.UrlMapping;
import com.urlShortener.UrlShortenerProject.service.UrlService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/urls")
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping
    public ResponseEntity<UrlResponse> createShortUrl(
            @Valid @RequestBody CreateUrlRequest request) {

        UrlResponse response = urlService.createShortUrl(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<UrlResponse> getUrlDetails(
            @PathVariable String shortCode) {

        return ResponseEntity.ok(
                urlService.getUrlDetails(shortCode)
        );
    }

    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> deleteUrl(
            @PathVariable String shortCode) {

        urlService.deleteUrl(shortCode);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/redirect/{shortCode}")
    public RedirectView redirect(
            @PathVariable String shortCode) {

        UrlMapping urlMapping =
                urlService.getUrlForRedirect(shortCode);

        return new RedirectView(urlMapping.getOriginalUrl());
    }
    
    @GetMapping
    public ResponseEntity<List<UrlResponse>> getAllUrls() {
        return ResponseEntity.ok(urlService.getAllUrls());
    }
}
