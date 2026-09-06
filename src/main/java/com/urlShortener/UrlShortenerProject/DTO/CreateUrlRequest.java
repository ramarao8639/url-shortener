package com.urlShortener.UrlShortenerProject.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateUrlRequest(

        @NotBlank(message = "Original URL is required")
        @Pattern(
                regexp = "^(https?://).+",
                message = "URL must start with http:// or https://"
        )
        String originalUrl
) {
}