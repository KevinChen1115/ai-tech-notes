package com.kevin.aitechnotes.controller;

import com.kevin.aitechnotes.dto.ApiResponse;
import com.kevin.aitechnotes.dto.PostDto;
import com.kevin.aitechnotes.entity.RawPost;
import com.kevin.aitechnotes.exception.ResourceNotFoundException;
import com.kevin.aitechnotes.repository.RawPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final RawPostRepository rawPostRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostDto>>> getAllPosts() {
        List<PostDto> posts = rawPostRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("查詢成功", posts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDto>> getPostById(@PathVariable UUID id) {
        RawPost post = rawPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 ID 為 " + id + " 的文章 "));
        return ResponseEntity.ok(ApiResponse.success("查詢成功", toDto(post)));
    }

    private PostDto toDto(RawPost post) {
        return new PostDto(
                post.getId(),
                post.getPlatform(),
                post.getAuthor(),
                post.getContent(),
                post.getUrl(),
                post.getScrapedAt(),
                post.getIsProcessed()
        );
    }
}
