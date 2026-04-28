package com.kevin.aitechnotes.controller;

import com.kevin.aitechnotes.dto.ApiResponse;
import com.kevin.aitechnotes.dto.NotifyResult;
import com.kevin.aitechnotes.service.DiscordNotifierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final DiscordNotifierService discordNotifierService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<NotifyResult>> sendPosts() {
        NotifyResult result = discordNotifierService.sendDailyDigest();
        return ResponseEntity.ok(ApiResponse.success("文章推送完成", result));
    }
}
