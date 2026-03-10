package com.kevin.aitechnotes.service;

import com.kevin.aitechnotes.entity.AiNote;
import com.kevin.aitechnotes.entity.RawPost;
import com.kevin.aitechnotes.repository.AiNoteRepository;
import com.kevin.aitechnotes.repository.RawPostRepository;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiProcessorService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.model-id}")
    private String geminiModelId;

    // 每次請求間隔（毫秒），Free Tier 限制 15 req/min = 4秒安全間距
    private static final long RATE_LIMIT_DELAY_MS = 4000;
    private static final long RETRY_503_DELAY_MS = 10_000; // 新增：503 等10秒
    private static final int MAX_RETRIES = 3; // 新增：最多重試3次

    private final RawPostRepository rawPostRepository;
    private final AiNoteRepository aiNoteRepository;
    private final ObjectMapper objectMapper; // Jackson，Spring Boot 自動注入

    public void processUnprocessedPosts() {
        // 撈出所有未處理的文章
        List<RawPost> unprocessedPosts = rawPostRepository.findByIsProcessed(false);
        log.info("找到 {} 篇未處理文章", unprocessedPosts.size());

        // 建立 Gemini 模型
        GoogleAiGeminiChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName(geminiModelId)
                .build();

        for (int i = 0; i < unprocessedPosts.size(); i++) {
            RawPost post = unprocessedPosts.get(i);
            try {
                // 除了第一層，每篇前都等待
                if (i > 0) {
                    log.info("等待 {}ms 避免觸發速率限制...", RATE_LIMIT_DELAY_MS);
                    Thread.sleep(RATE_LIMIT_DELAY_MS);
                }
                // 改成呼叫有重試機制的方法
                String response = callGeminiWithRetry(model, post);
                saveAiNote(post, response);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 標準作法，恢復中斷狀態
                log.error("執行緒被中斷", e);
                break;
            } catch (Exception e) {
                log.error("AI 分析失敗，文章ID: {}", post.getId(), e);
            }
        }
        log.info("AI 處理完成！");
    }

    private String callGeminiWithRetry(GoogleAiGeminiChatModel model, RawPost post)
            throws InterruptedException {

        String prompt = bulidPrompt(post); // 把 prompt 獨立成方法，更乾淨

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                log.info("呼叫Gemini，文章ID: {}，第 {}/{} 次", post.getId(), attempt, MAX_RETRIES);
                return model.chat(prompt); // 成功就直接回傳
            } catch (Exception e) {
                String errorMsg = e.getMessage() != null ? e.getMessage() : "";

                if (attempt == MAX_RETRIES) {
                    // 已達上限，往上拋例外
                    throw e;
                }

                if (errorMsg.contains("429") || errorMsg.contains("RESOURSE_EXHAUSTED")) {
                    // 429：解析等待秒數，預設35秒
                    long waitSeconds = parseRetryAfter(errorMsg);
                    log.warn("觸發 429，等待 {}秒 後重試...", waitSeconds);
                    Thread.sleep(waitSeconds * 1000);
                } else if (errorMsg.contains("503") || errorMsg.contains("UNAVAILABLE")) {
                    // 503：伺服器忙碌，等10秒
                    log.warn("觸發 503，等待 {}ms 後重試...", RETRY_503_DELAY_MS);
                    Thread.sleep(RETRY_503_DELAY_MS);
                } else {
                    // 其他錯誤，不重試，直接往上拋
                    throw e;
                }
            }
        }

        // 理論上不會到這裡（for 迴圈內一定會 return or throw)
        // 但 Java 編譯器需要這行
        throw new RuntimeException("Gemini 呼叫失敗，已達重試上線");
    }

    // 新增：從錯誤訊息解析應等待的秒數
    private long parseRetryAfter(String errorMsg) {
        // 錯誤訊息通常包含 "retry in 35s" 或 "retryDelay: 35s"
        try {
            int retryIndex = errorMsg.indexOf("retry");
            if (retryIndex != -1) {
                String after = errorMsg.substring(retryIndex);
                // 找數字
                StringBuilder numStr = new StringBuilder();
                for (char c : after.toCharArray()) {
                    if (Character.isDigit(c)) {
                        numStr.append(c);
                    } else if (numStr.length() > 0) {
                        break; // 數字結束了
                    }
                }
                if (!numStr.isEmpty()) {
                    return Long.parseLong(numStr.toString()) + 5; // 多等5秒緩衝
                }
            }
        } catch (Exception e) {
            log.warn("無法解析 retryAfter，使用預設值 35 秒");
        }
        return 35; // 預設值
    }

    // 新增：把 prompt 獨立出來，analyzePost 拆成兩個職責
    private String bulidPrompt(RawPost post) {
        return """
                    你是一個技術內容分析師，請分析以下文章：
                
                    內容：%s
                    來源：%s
                
                    請務必嚴格遵守以下規則：
                    1. 必須只回覆合法的 JSON 格式。
                    2. 絕對不要使用 Markdown 語法包裝（不要加上 ```json 和 ```）。
                    3. 不要輸出任何 JSON 以外的解釋性文字。
                
                    請用以下 JSON 格式回覆：
                    {
                      "is_valuable": true或false,
                      "tags": ["標籤1", "標籤2", "標籤3"],
                      "summary": "繁體中文摘要，50字以內"
                    }
                
                    判斷標準：
                    - is_valuable: 是否與軟體開發、AI、後端架構相關
                    - tags: 提取 2-3 個關鍵技術標籤（請務必輸出為 JSON 陣列格式）
                    - summary: 用繁體中文說明這篇文章在討論什麼
                """.formatted(post.getContent(), post.getPlatform());
    }

    private void saveAiNote(RawPost post, String aiResponse) {
        try {
            // 清除 markdown 符號
            String cleanJson = aiResponse
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            // Jackson 解析
            JsonNode root = objectMapper.readValue(cleanJson, JsonNode.class);
            boolean isValuable = root.path("is_valuable").asBoolean(false);
            String tags = "";
            JsonNode tagsNode = root.path("tags");
            if (tagsNode.isArray()) {
                List<String> tagList = new ArrayList<>();
                tagsNode.forEach(tag -> tagList.add(tag.asText()));
                tags = String.join(",", tagList);
            }
            String summary = root.path("summary").asText("");

            AiNote note = new AiNote();
            note.setPost(post);
            note.setIsValuable(isValuable);
            note.setTags(tags);
            note.setSummary(summary);
            aiNoteRepository.save(note);

            // 標記文章已處理
            post.setIsProcessed(true);
            rawPostRepository.save(post);

            log.info("已儲存 AI 筆記：{}", summary);
        } catch (Exception e) {
            log.error("儲存 AI 筆記失敗", e);
        }
    }
}
