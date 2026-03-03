package com.kevin.aitechnotes.controller;

import com.kevin.aitechnotes.entity.RawPost;
import com.kevin.aitechnotes.service.HackerNewsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/collector")
public class HackerNewsController {

    private final HackerNewsService hackerNewsService;

    public HackerNewsController(HackerNewsService hackerNewsService){
        this.hackerNewsService = hackerNewsService;
    }

    // 手動觸發抓取
    @PostMapping("/fetch/hackernews")
    public ResponseEntity<Map<String, String>> fetchHackerNews() {
        hackerNewsService.fetchAndSaveTopStories();
        return ResponseEntity.ok(Map.of("message", "抓取完成，請查看 log"));
    }

    // 健康檢查
    @GetMapping("health")
    public ResponseEntity<Map<String, String>> health(){
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
