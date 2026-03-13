package com.kevin.aitechnotes.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskService {

    private final HackerNewsService hackerNewsService;
    private final AiProcessorService aiProcessorService;
    private final DiscordNotifierService discordNotifierService;

    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Taipei")
    public void dailyPipeline() {
        log.info("=== 每日排程開始 ===");

        log.info("[1/3] 抓取 Hacker News 文章...");
        hackerNewsService.fetchAndSaveTopStories();

        log.info("[2/3] AI分析...");
        aiProcessorService.processUnprocessedPosts();

        log.info("[3/3] 推送 Discord");
        discordNotifierService.sendDailyDigest();

        log.info("=== 每日排程完成 ===");
    }
}
