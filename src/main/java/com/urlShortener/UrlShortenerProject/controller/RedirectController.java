package com.urlShortener.UrlShortenerProject.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.urlShortener.UrlShortenerProject.entity.UrlMapping;
import com.urlShortener.UrlShortenerProject.service.UrlService;

@RestController
public class RedirectController {

    private final UrlService urlService;

    public RedirectController(UrlService urlService) {
        this.urlService = urlService;
    }

    @GetMapping("/{shortCode}")
    public RedirectView redirect(
            @PathVariable String shortCode) {

        UrlMapping urlMapping =
                urlService.getUrlForRedirect(shortCode);

        return new RedirectView(
                urlMapping.getOriginalUrl()
        );
    }
}