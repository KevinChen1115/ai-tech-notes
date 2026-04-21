package com.kevin.aitechnotes.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record NoteDto(
        UUID id,
        UUID postId,
        String postUrl,
        Boolean isValuable,
        String tags,
        String summary,
        LocalDateTime createdAt
) {}
