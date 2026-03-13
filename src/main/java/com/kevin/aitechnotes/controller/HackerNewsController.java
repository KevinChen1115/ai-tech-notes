package com.kevin.aitechnotes.controller;

import com.kevin.aitechnotes.entity.AiNote;
import com.kevin.aitechnotes.entity.RawPost;
import com.kevin.aitechnotes.repository.AiNoteRepository;
import com.kevin.aitechnotes.service.AiProcessorService;
import com.kevin.aitechnotes.service.DiscordNotifierService;
import com.kevin.aitechnotes.service.HackerNewsService;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final AiProcessorService aiProcessorService;
    private final DiscordNotifierService discordNotifierService;
    private final AiNoteRepository aiNoteRepository;


    public HackerNewsController(HackerNewsService hackerNewsService,
                                AiProcessorService aiProcessorService,
                                DiscordNotifierService discordNotifierService,
                                AiNoteRepository aiNoteRepository){
        this.hackerNewsService = hackerNewsService;
        this.aiProcessorService = aiProcessorService;
        this.discordNotifierService = discordNotifierService;
        this.aiNoteRepository = aiNoteRepository;
    }

    @PostMapping("/notify/discord")
    public String notifyDiscord() {
        discordNotifierService.sendDailyDigest();
        return "Discord 推送完成！";
    }

    // 手動觸發抓取
    @PostMapping("/fetch/hackernews")
    public ResponseEntity<Map<String, String>> fetchHackerNews() {
        hackerNewsService.fetchAndSaveTopStories();
        return ResponseEntity.ok(Map.of("message", "抓取完成，請查看 log"));
    }

    @PostMapping("/process/ai")
    public ResponseEntity<Map<String, String>> processWithAi(){
        aiProcessorService.processUnprocessedPosts();
        return ResponseEntity.ok(Map.of("message", "AI 處理完成，請查看 log"));
    }
    // 健康檢查
    @GetMapping("health")
    public ResponseEntity<Map<String, String>> health(){
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
