package com.kevin.aitechnotes.dto;

import com.kevin.aitechnotes.entity.RawPost;

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
) {
    public static PostDto fromEntity(RawPost post) {
        return new PostDto(
                post.getId(),
                post.getPlatform(),
                post.getAuthor(),
                post.getContent(),
                post.getUrl(),
                post.getScrapedAt(),
                post.getIsProcessed()
        );
    }
}
