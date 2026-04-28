package com.kevin.aitechnotes.service;

import com.kevin.aitechnotes.controller.NotificationController;
import com.kevin.aitechnotes.dto.NotifyResult;
import com.kevin.aitechnotes.entity.AiNote;
import com.kevin.aitechnotes.repository.AiNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordNotifierService {

    @Value("${discord.webhook.url}")
    private String webhookUrl;

    private final AiNoteRepository aiNoteRepository;
    private final RestClient restClient = RestClient.create();

    public NotifyResult sendDailyDigest(){
        // 只推送 AI 判斷有價值的文章
        List<AiNote> notes = aiNoteRepository.findAllWithPostOrderByCreatedAtDesc();
        List<AiNote> valuableNotes = notes.stream()
                .filter(AiNote::getIsValuable)
                .toList();

        if (valuableNotes.isEmpty()) {
            log.info("今日沒有值得推送的文章");
            return new NotifyResult(0, 0);
        }

        String content = buildMessage(valuableNotes);
        sendToDiscord(content);
        return new NotifyResult(valuableNotes.size(), valuableNotes.size());
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
