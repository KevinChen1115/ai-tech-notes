package com.kevin.aitechnotes.service;


import com.kevin.aitechnotes.entity.RawPost;
import com.kevin.aitechnotes.repository.RawPostRepository;
import com.kevin.aitechnotes.util.UrlNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.*;

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
        List<RawPost> fetchedPosts = new ArrayList<>();
        for (int i = 0; i < Math.min(30, storyIds.length); i++) {
            try {
                RawPost post = fetchStory(storyIds[i]);
                if (post != null) {
                    fetchedPosts.add(post);
                }
            } catch (Exception e) {
                log.error("抓取文章失敗，ID: {}", storyIds[i], e);
            }
        }
        filterAndSave(fetchedPosts);
        log.info("抓取完成！");
    }

    public void filterAndSave(List<RawPost> fetchedPosts) {
        int found = fetchedPosts.size();

        // 正規化所有 URL
        for(RawPost post : fetchedPosts){
            post.setUrl(UrlNormalizer.normalize(post.getUrl()));
        }

        // Set 去掉批內重複
        Set<String> seenUrls = new HashSet<>();
        List<RawPost> uniquePosts = new ArrayList<>();

        for(RawPost post : fetchedPosts){
            if(seenUrls.add(post.getUrl())) {
                uniquePosts.add(post);
            }
        }

        // 拿剩下 URL 去查 DB
        List<String> urlsToCheck = new ArrayList<>();
        for (RawPost post : uniquePosts) {
            urlsToCheck.add(post.getUrl());
        }

        List<RawPost> existingPosts = rawPostRepository.findAllByUrlIn(urlsToCheck);

        Set<String> existingUrls = new HashSet<>();
        for (RawPost post : existingPosts) {
            existingUrls.add(post.getUrl());
        }

        // 濾掉 DB 已存在的
        List<RawPost> newPosts = new ArrayList<>();
        for (RawPost post : uniquePosts) {
            if (!existingUrls.contains(post.getUrl())){
                newPosts.add(post);
            }
        }

        // 存入 DB + 印日誌
        int newCount = newPosts.size();
        int skipped = found - newCount;
        log.info("Found {}, New {}, Skipped {}", found, newCount, skipped);

        if (!newPosts.isEmpty()) {
            rawPostRepository.saveAll(newPosts);
        }
    }

    private RawPost fetchStory(int storyId){
        // 抓取單篇文章的詳細內容
        Map<String, Object> story = restClient.get()
                .uri("/item/{id}.json", storyId)
                .retrieve()
                .body(Map.class);

        if (story == null || story.get("title") == null) return null;

        RawPost post = new RawPost();
        post.setPlatform("HackerNews");
        post.setAuthor((String) story.get("by"));
        post.setContent((String) story.get("title"));
        post.setUrl((String) story.get("url"));
        post.setScrapedAt(LocalDateTime.now());
        post.setIsProcessed(false);
        return post;
    }
}
