package com.kevin.aitechnotes.controller;

import com.kevin.aitechnotes.dto.ApiResponse;
import com.kevin.aitechnotes.dto.CollectResult;
import com.kevin.aitechnotes.dto.PostDto;
import com.kevin.aitechnotes.entity.RawPost;
import com.kevin.aitechnotes.exception.ResourceNotFoundException;
import com.kevin.aitechnotes.repository.RawPostRepository;
import com.kevin.aitechnotes.service.HackerNewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final RawPostRepository rawPostRepository;
    private final HackerNewsService hackerNewsService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostDto>>> getAllPosts() {
        List<PostDto> posts = rawPostRepository.findAll()
                .stream()
                .map(PostDto::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("查詢成功", posts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDto>> getPostById(@PathVariable UUID id) {
        RawPost post = rawPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 ID 為 " + id + " 的文章 "));
        return ResponseEntity.ok(ApiResponse.success("查詢成功", PostDto.fromEntity(post)));
    }

    @PostMapping("collect")
    public ResponseEntity<ApiResponse<CollectResult>> collect() {
        CollectResult result = hackerNewsService.fetchAndSaveTopStories();
        return ResponseEntity.ok(ApiResponse.success("抓取完成", result));
    }
}
