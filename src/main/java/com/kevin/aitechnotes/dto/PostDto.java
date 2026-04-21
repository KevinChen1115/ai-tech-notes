package com.kevin.aitechnotes.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostDto(
        UUID id,
        String platform,
        String author,
        String content,
        String url,
        LocalDateTime scrapeAt,
        Boolean isProcessed
) {}
