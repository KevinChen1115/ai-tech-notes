package com.kevin.aitechnotes.controller;

import com.kevin.aitechnotes.dto.ApiResponse;
import com.kevin.aitechnotes.dto.NoteDto;
import com.kevin.aitechnotes.dto.ProcessResult;
import com.kevin.aitechnotes.exception.ResourceNotFoundException;
import com.kevin.aitechnotes.repository.AiNoteRepository;
import com.kevin.aitechnotes.service.AiProcessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final AiNoteRepository aiNoteRepository;
    private final AiProcessorService aiProcessorService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NoteDto>>> getAllNotes() {
        List<NoteDto> notes = aiNoteRepository.findAllWithPostOrderByCreatedAtDesc()
                .stream()
                .map(NoteDto::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("查詢成功", notes));
    }

    @GetMapping({"/{id}"})
    public ResponseEntity<ApiResponse<NoteDto>> getNoteById(@PathVariable UUID id) {
        var note = aiNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "找不到 ID 為 " + id + " 的筆記"));
        return ResponseEntity.ok(ApiResponse.success("查詢成功", NoteDto.fromEntity(note)));
    }

    @PostMapping("/process")
    public ResponseEntity<ApiResponse<ProcessResult>> process() {
        ProcessResult result = aiProcessorService.processUnprocessedPosts();
        return ResponseEntity.ok(ApiResponse.success("AI 分析完成", result));
    }
}
