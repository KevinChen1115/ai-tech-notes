package com.kevin.aitechnotes.service;

import com.kevin.aitechnotes.entity.RawPost;
import com.kevin.aitechnotes.repository.RawPostRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest // 啟動整個 Spring 應用
@ActiveProfiles("test") // 用 application-test.yml 的設定 (H2)
@Transactional // 每個測試結束後自動 rollback，保持乾淨
public class FilterAndSaveIntegrationTest {

    @Autowired
    HackerNewsService hackerNewsService;

    @Autowired
    RawPostRepository rawPostRepository;

    @Test
    void 三篇新文章應該全部存入(){
        // Arrange
        RawPost post1 = new RawPost();
        post1.setUrl("https://example.com/article1");
        post1.setPlatform("hackernews");
        post1.setAuthor("testuser");
        post1.setContent("Test content");
        post1.setScrapedAt(LocalDateTime.now());
        post1.setIsProcessed(false);

        RawPost post2 = new RawPost();
        post2.setUrl("https://example.com/article2");
        post2.setPlatform("hackernews");
        post2.setAuthor("testuser");
        post2.setContent("Test content");
        post2.setScrapedAt(LocalDateTime.now());
        post2.setIsProcessed(false);

        RawPost post3 = new RawPost();
        post3.setUrl("https://example.com/article3");
        post3.setPlatform("hackernews");
        post3.setAuthor("testuser");
        post3.setContent("Test content");
        post3.setScrapedAt(LocalDateTime.now());
        post3.setIsProcessed(false);

        List<RawPost> fetchedPosts = List.of(post1, post2, post3);

        // Act
        hackerNewsService.filterAndSave(fetchedPosts);

        // Assert
        assertEquals(3, rawPostRepository.count());
    }

    @Test
    void 三篇新文章存兩次應該全部存入一次總數為三(){
        // Arrange
        RawPost post1 = new RawPost();
        post1.setUrl("https://example.com/article1");
        post1.setPlatform("hackernews");
        post1.setAuthor("testuser");
        post1.setContent("Test content");
        post1.setScrapedAt(LocalDateTime.now());
        post1.setIsProcessed(false);

        RawPost post2 = new RawPost();
        post2.setUrl("https://example.com/article2");
        post2.setPlatform("hackernews");
        post2.setAuthor("testuser");
        post2.setContent("Test content");
        post2.setScrapedAt(LocalDateTime.now());
        post2.setIsProcessed(false);

        RawPost post3 = new RawPost();
        post3.setUrl("https://example.com/article3");
        post3.setPlatform("hackernews");
        post3.setAuthor("testuser");
        post3.setContent("Test content");
        post3.setScrapedAt(LocalDateTime.now());
        post3.setIsProcessed(false);

        List<RawPost> fetchedPosts = List.of(post1, post2, post3);

        // Act
        hackerNewsService.filterAndSave(fetchedPosts);
        hackerNewsService.filterAndSave(fetchedPosts);

        // Assert
        assertEquals(3, rawPostRepository.count());
    }

    @Test
    void DB已有正規化URL不會重複存(){
        // Arrange
        RawPost post1 = new RawPost();
        post1.setUrl("https://example.com/article1");
        post1.setPlatform("hackernews");
        post1.setAuthor("testuser");
        post1.setContent("Test content");
        post1.setScrapedAt(LocalDateTime.now());
        post1.setIsProcessed(false);

        RawPost post2 = new RawPost();
        post2.setUrl("https://Example.COM/article1/");
        post2.setPlatform("hackernews");
        post2.setAuthor("testuser");
        post2.setContent("Test content");
        post2.setScrapedAt(LocalDateTime.now());
        post2.setIsProcessed(false);

        // Act
        hackerNewsService.filterAndSave(List.of(post1));
        hackerNewsService.filterAndSave(List.of(post2));

        // Assert
        assertEquals(1, rawPostRepository.count());
    }
}
