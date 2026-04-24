package com.kevin.aitechnotes.controller;

import com.kevin.aitechnotes.dto.ProcessResult;
import com.kevin.aitechnotes.entity.AiNote;
import com.kevin.aitechnotes.entity.RawPost;
import com.kevin.aitechnotes.repository.AiNoteRepository;
import com.kevin.aitechnotes.repository.RawPostRepository;
import com.kevin.aitechnotes.service.AiProcessorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RawPostRepository rawPostRepository;

    @Autowired
    private AiNoteRepository aiNoteRepository;

    @MockitoBean
    private AiProcessorService aiProcessorService;

    @Test
    @DisplayName("GET /api/notes - 回傳所有 AI 筆記")
    void shouldReturnAllNotes() throws Exception {
        // Arrange
        aiNoteRepository.deleteAll();
        rawPostRepository.deleteAll();

        RawPost post = new RawPost();
        post.setPlatform("hackernews");
        post.setAuthor("test-author");
        post.setContent("test content");
        post.setUrl("https://example.com/note-test");
        post.setScrapedAt(LocalDateTime.now());
        post.setIsProcessed(true);
        rawPostRepository.save(post);

        AiNote note = new AiNote();
        note.setPost(post);
        note.setIsValuable(true);
        note.setTags("Java,AI");
        note.setSummary("測試摘要");
        aiNoteRepository.save(note);
    }

    @Test
    @DisplayName("POST /api/notes/{id} - 找不到筆記回傳 404")
    void shouldReturn404WhenNoteNotFound() throws Exception {
        mockMvc.perform(get("/api/notes/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    @DisplayName("POST /api/notes/process - 觸發 AI 分析，回傳結果統計")
    void shouldProcessAndReturnResult() throws Exception {
        // Arrange
        when(aiProcessorService.processUnprocessedPosts())
                .thenReturn(new ProcessResult(10, 7, 3));

        // Act & Assert
        mockMvc.perform(post("/api/notes/process"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.total").value(10))
                .andExpect(jsonPath("$.data.valuable").value(7))
                .andExpect(jsonPath("$.data.notValuable").value(3));
    }
}
