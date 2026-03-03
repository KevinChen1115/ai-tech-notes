package com.kevin.aitechnotes.service;


import com.kevin.aitechnotes.entity.RawPost;
import com.kevin.aitechnotes.repository.RawPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j  // Lombok自動產生 log 物件，方便印 log
@Service // 告訴 Spring 這是Service 層的元件，會被自動管理
@RequiredArgsConstructor // Lombok自動產生建構子，處理依賴注入
public class HackerNewsService {
    private final RawPostRepository rawPostRepository;

    // RestClient 是 Spring Boot 4,0 推薦的 HTTP 客戶端
    private final RestClient restClient = RestClient.create("https://hacker-news.firebaseio.com/v0");

    public void fetchAndSaveTopStories() {
        log.info("開始抓取 Hacker News 文章...");

        //第一步：抓取前 30 篇熱門文章的 ID 列表
        int[] storyIds = restClient.get()
                .uri("/topstories.json")
                .retrieve()
                .body(int[].class);

        if (storyIds == null) return;

        //第二步：只取前 30 篇
        for (int i = 0; i < Math.min(30, storyIds.length); i++) {
            try {
                fetchAndSaveStory(storyIds[i]);
            } catch (Exception e) {
                log.error("抓取文章失敗，ID: {}", storyIds[i], e);
            }
        }
        log.info("抓取完成！");
    }

    private void fetchAndSaveStory(int storyId){
        // 抓取單篇文章的詳細內容
        Map<String, Object> story = restClient.get()
                .uri("/item/{id}.json", storyId)
                .retrieve()
                .body(Map.class);

        if (story == null || story.get("title") == null) return;

        RawPost post = new RawPost();
        post.setPlatform("HackerNews");
        post.setAuthor((String) story.get("by"));
        post.setContent((String) story.get("title"));
        post.setUrl((String) story.get("url"));
        post.setScrapedAt(LocalDateTime.now());
        post.setIsProcessed(false);

        rawPostRepository.save(post);
        log.info("已儲存文章: {}", story.get("title"));

    }
}
