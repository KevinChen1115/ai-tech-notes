package com.kevin.aitechnotes.dto;

import com.kevin.aitechnotes.entity.AiNote;

import java.time.LocalDateTime;
import java.util.UUID;

public record NoteDto(
        UUID id,
        UUID postId,
        String postUrl,
        Boolean isValuable,
        String tags,
        String summary,
        LocalDateTime createdAt
) {
    public static NoteDto fromEntity(AiNote note) {
        return new NoteDto(
                note.getId(),
                note.getPost().getId(),
                note.getPost().getUrl(),
                note.getIsValuable(),
                note.getTags(),
                note.getSummary(),
                note.getCreatedAt()
        );
    }
}
