package com.kevin.aitechnotes.service;

import com.kevin.aitechnotes.entity.AiNote;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DiscordNotifierService {

    @Value("${discord.webhook.url}")
    private String webhookUrl;

    private final RestClient restClient = RestClient.create();

    public void sendDailyDigest(List<AiNote> notes){
        // 只推送 AI 判斷有價值的文章
        List<AiNote> valuableNotes = notes.stream()
                .filter(AiNote::getIsValuable)
                .toList();

        if (valuableNotes.isEmpty()) {
            log.info("今日沒有值得推送的文章");
            return;
        }

        String content = buildMessage(valuableNotes);
        sendToDiscord(content);
    }

    private String buildMessage(List<AiNote> notes) {
        StringBuilder sb = new StringBuilder();
        sb.append("##今日技術摘要\n\n");

        for (AiNote note : notes){
            sb.append("**").append(note.getPost().getContent()).append("**\n");
            sb.append("🏷️ ").append(note.getTags()).append("\n");
            sb.append("📝 ").append(note.getSummary()).append("\n");
            sb.append("🔗 ").append(note.getPost().getUrl()).append("\n");
            sb.append("\n---\n\n");
        }

        return sb.toString();
    }

    private void sendToDiscord(String content) {
        // Discord 單則訊息上限 2000 字，超過要切割
        if (content.length() > 2000) {
            content = content.substring(0,1997) + "...";
        }

        Map<String, String> body = Map.of("content", content);

        restClient.post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();

        log.info("Discord 推送成功！");
    }
}
