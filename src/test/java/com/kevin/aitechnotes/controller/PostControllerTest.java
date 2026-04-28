package com.kevin.aitechnotes.controller;

import com.kevin.aitechnotes.entity.RawPost;
import com.kevin.aitechnotes.repository.RawPostRepository;
import com.kevin.aitechnotes.service.HackerNewsService;
import com.kevin.aitechnotes.dto.CollectResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.rmi.server.ExportException;
import java.time.LocalDateTime;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RawPostRepository rawPostRepository;

    @MockitoBean
    private HackerNewsService hackerNewsService;

    @Test
    @DisplayName("GET /api/posts - 回傳所有文章，格式為統一 ApiResponse")
    void shouldReturnAllPosts() throws Exception {
        // Arrange
        rawPostRepository.deleteAll();
        RawPost post = new RawPost();
        post.setPlatform("hackernews");
        post.setAuthor("test-author");
        post.setContent("test content");
        post.setUrl("https://example.com/test");
        post.setScrapedAt(LocalDateTime.now());
        rawPostRepository.save(post);

        // Act & Assert
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].platform").value("hackernews"))
                .andExpect(jsonPath("$.data[0].url").value("https://example.com/test"));
    }
    @Test
    @DisplayName("GET /api/posts/{id} - 找到文章回傳 200")
    void shouldReturnPostById() throws Exception {
        // Arrange
        rawPostRepository.deleteAll();
        RawPost post = new RawPost();
        post.setPlatform("hackernews");
        post.setAuthor("test-author");
        post.setContent("test content");
        post.setUrl("https://example.com/test2");
        post.setScrapedAt(LocalDateTime.now());
        RawPost saved = rawPostRepository.save(post);

        // Act & Assert
        mockMvc.perform(get("/api/posts/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(saved.getId().toString()));
    }

    @Test
    @DisplayName("GET /api/posts/{id} - 找不到文章回傳 404")
    void shouldReturn404WhenPostNotFound() throws Exception {
        mockMvc.perform(get("/api/posts/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    @DisplayName("POST /api/posts/collect - 觸發抓取，回傳結果統計")
    void shouldCollectAndReturnResult() throws Exception {
        // Arrange
        when(hackerNewsService.fetchAndSaveTopStories())
                .thenReturn(new CollectResult(30, 25, 5));

        // Act & Assert
        mockMvc.perform(post("/api/posts/collect"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.found").value(30))
                .andExpect(jsonPath("$.data.saved").value(25))
                .andExpect(jsonPath("$.data.skipped").value(5));
    }
}
