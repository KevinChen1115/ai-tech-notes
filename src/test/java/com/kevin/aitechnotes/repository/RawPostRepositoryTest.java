package com.kevin.aitechnotes.repository;


import com.kevin.aitechnotes.entity.RawPost;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static  org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RawPostRepositoryTest {

    @Autowired
    private RawPostRepository rawPostRepository;

    @Test
    @DisplayName("findAllByUrlIn - 查到匹配的 URL，回傳對應的 RawPost")
    void shouldReturnMatchingPosts() {
        // Given: DB 裡有 3 筆資料
        RawPost post1 = createPost("https://example.com/article-1");
        RawPost post2 = createPost("https://example.com/article-2");
        RawPost post3 = createPost("https://example.com/article-3");
        rawPostRepository.saveAll(List.of(post1, post2, post3));

        // When: 查詢其中 2 個 URL
        List<String> urlsToCheck = List.of(
                "https://example.com/article-1",
                "https://example.com/article-3"
        );
        List<RawPost> result = rawPostRepository.findAllByUrlIn(urlsToCheck);

        // Then: 應該回傳 2 筆
        assertThat(result).hasSize(2);
        assertThat(result).extracting(RawPost::getUrl)
                .containsExactlyInAnyOrder(
                        "https://example.com/article-1",
                        "https://example.com/article-3"
                );
    }

    @Test
    @DisplayName("findAllByUrlIn - 沒有匹配的 URL，回傳空 List")
    void shouldReturnEmptyWhenNoMatch() {
        // Given: DB 裡有資料
        rawPostRepository.save(createPost("https://example.com/exists"));

        // When: 查的 URL 都不在 DB 裡
        List<String> urlsToCheck = List.of(
                "https://example.com/not-exists-1",
                "https://example.com/not-exists-2"
        );
        List<RawPost> result = rawPostRepository.findAllByUrlIn(urlsToCheck);

        // Then
        assertThat(result).isEmpty();
    }

    // === 輔助方法 ===
    private RawPost createPost(String url) {
        RawPost post = new RawPost();
        post.setUrl(url);
        post.setPlatform("hackernews");
        post.setAuthor("testuser");
        post.setContent("Test content");
        post.setScrapedAt(LocalDateTime.now());
        post.setIsProcessed(false);
        return post;
    }
}
