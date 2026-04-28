package com.kevin.aitechnotes.controller;

import com.kevin.aitechnotes.dto.NotifyResult;
import com.kevin.aitechnotes.service.DiscordNotifierService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DiscordNotifierService discordNotifierService;

    @Test
    @DisplayName("POST /api/notifications/send - 成功推送文章")
    void shouldReturn200AndNotifyResult() throws Exception {
        // Arrange
        when(discordNotifierService.sendDailyDigest())
                .thenReturn(new NotifyResult(3, 3));

        // Act & Assert
        mockMvc.perform(post("/api/notifications/send"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.sent").value(3));
    }

    @Test
    @DisplayName("POST /api/notifications/send - 沒有文章可推")
    void shouldReturn200WhenPostNotsend() throws Exception {
        // Arrange
        when(discordNotifierService.sendDailyDigest())
                .thenReturn(new NotifyResult(0, 0));

        // Act & Assert
        mockMvc.perform(post("/api/notifications/send"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.sent").value(0));
    }
}
