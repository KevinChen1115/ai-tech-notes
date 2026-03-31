package com.kevin.aitechnotes.service;

import com.kevin.aitechnotes.entity.RawPost;
import com.kevin.aitechnotes.repository.RawPostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class HackerNewsServiceTest {

    @Mock
    RawPostRepository rawPostRepository;

    @InjectMocks
    HackerNewsService hackerNewsService;

    // 測一
    @Test
    void 全部都是新文章_應該全部存入(){
        // Arrange（準備）- 造 3 篇文章
        RawPost post1 = new RawPost();
        post1.setUrl("https://example.com/article1");

        RawPost post2 = new RawPost();
        post2.setUrl("https://example.com/article2");

        RawPost post3 = new RawPost();
        post3.setUrl("https://example.com/article3");

        List<RawPost> fetchedPosts = List.of(post1, post2, post3);

        // Arrange (劇本）- DB 裡沒有任何已存在的文章
        when(rawPostRepository.findAllByUrlIn(anyList()))
                .thenReturn(Collections.emptyList());

        // Act (執行)
        hackerNewsService.filterAndSave(fetchedPosts);

        // Assert (驗證) - 應該存 3 篇
        verify(rawPostRepository).saveAll(argThat(list ->
                ((List<?>) list).size() == 3
        ));
    }
    // 測二
    @Test
    void 一篇DB已存在_應該存兩篇跳一篇(){

        RawPost post1 = new RawPost();
        post1.setUrl("https://example.com/article1");

        RawPost post2 = new RawPost();
        post2.setUrl("https://example.com/article2");

        RawPost post3 = new RawPost();
        post3.setUrl("https://example.com/article3");

        List<RawPost> fetchedPosts = List.of(post1, post2, post3);

        //Arrange (劇本) - DB 裡已經有 article1
        RawPost existingPost = new RawPost();
        existingPost.setUrl("https://example.com/article1");

        when(rawPostRepository.findAllByUrlIn(anyList()))
                .thenReturn(List.of(existingPost));

        // Act (執行)
        hackerNewsService.filterAndSave(fetchedPosts);

        // Assert (驗證) - 應該存 2 篇
        verify(rawPostRepository).saveAll(argThat(list ->
                ((List<?>) list).size() == 2
        ));
    }
    // 測三
    @Test
    void 抓到三篇中兩篇重複_應該存兩篇跳一篇(){

        RawPost post1 = new RawPost();
        post1.setUrl("https://example.com/article1");

        RawPost post2 = new RawPost();
        post2.setUrl("https://example.com/article1");

        RawPost post3 = new RawPost();
        post3.setUrl("https://example.com/article2");

        List<RawPost> fetchedPosts = List.of(post1, post2, post3);

        //Arrange (劇本) - DB 裡沒有任何已存在的文章
        when(rawPostRepository.findAllByUrlIn(anyList()))
                .thenReturn(Collections.emptyList());

        // Act (執行)
        hackerNewsService.filterAndSave(fetchedPosts);

        // Assert (驗證) - 應該存 2 篇
        verify(rawPostRepository).saveAll(argThat(list ->
                ((List<?>) list).size() == 2
        ));
    }
    // 測四
    @Test
    void 抓到兩篇但一篇URL沒正規化_應該存一篇跳一篇(){

        RawPost post1 = new RawPost();
        post1.setUrl("https://Example.COM/article1/");

        RawPost post2 = new RawPost();
        post2.setUrl("https://example.com/article2");

        List<RawPost> fetchedPosts = List.of(post1, post2);

        //Arrange (劇本) - DB 裡有 article1 (乾淨版)
        // DB 裡有 article1（乾淨版本）
        RawPost existingPost = new RawPost();
        existingPost.setUrl("https://example.com/article1");

        when(rawPostRepository.findAllByUrlIn(anyList()))
                .thenReturn(List.of(existingPost));

        // Act (執行)
        hackerNewsService.filterAndSave(fetchedPosts);

        // Assert (驗證) - 正規化後 post1 跟 DB 碰撞 → 只存 post2
        verify(rawPostRepository).saveAll(argThat(list ->
                ((List<?>) list).size() == 1
        ));
    }
    // 測五
    @Test
    void 抓到零篇_應該存零篇(){

        List<RawPost> fetchedPosts = List.of();

        //Arrange (劇本) - DB 裡沒有任何已存在的文章
        when(rawPostRepository.findAllByUrlIn(anyList()))
                .thenReturn(Collections.emptyList());

        // Act (執行)
        hackerNewsService.filterAndSave(fetchedPosts);

        // Assert (驗證) - 應該存 0 篇，不呼叫 saveAll
        verify(rawPostRepository, never()).saveAll(any());
    }
    // 測六
    @Test
    void 抓到三篇但三篇重複_應該存零篇(){

        RawPost post1 = new RawPost();
        post1.setUrl("https://example.com/article1");

        RawPost post2 = new RawPost();
        post2.setUrl("https://example.com/article2");

        RawPost post3 = new RawPost();
        post3.setUrl("https://example.com/article3");

        List<RawPost> fetchedPosts = List.of(post1, post2, post3);

        //Arrange (劇本) - DB 有三篇已存在的文章
        RawPost existingPost1 = new RawPost();
        existingPost1.setUrl("https://example.com/article1");

        RawPost existingPost2 = new RawPost();
        existingPost2.setUrl("https://example.com/article2");

        RawPost existingPost3 = new RawPost();
        existingPost3.setUrl("https://example.com/article3");

        List<RawPost> existPosts = List.of(existingPost1, existingPost2, existingPost3);

        when(rawPostRepository.findAllByUrlIn(anyList()))
                .thenReturn(existPosts);

        // Act (執行)
        hackerNewsService.filterAndSave(fetchedPosts);

        // Assert (驗證) - 應該存 0 篇
        verify(rawPostRepository, never()).saveAll(any());
    }
    // 測七
    @Test
    void 抓到三篇但兩篇重複資料庫也重複兩篇的URL_應該存一篇(){

        RawPost post1 = new RawPost();
        post1.setUrl("https://example.com/article1");

        RawPost post2 = new RawPost();
        post2.setUrl("https://example.com/article1");

        RawPost post3 = new RawPost();
        post3.setUrl("https://example.com/article2");

        List<RawPost> fetchedPosts = List.of(post1, post2, post3);

        //Arrange (劇本) - DB 有兩篇已存在的文章
        RawPost existingPost = new RawPost();
        existingPost.setUrl("https://example.com/article1");

        when(rawPostRepository.findAllByUrlIn(anyList()))
                .thenReturn(List.of(existingPost));

        // Act (執行)
        hackerNewsService.filterAndSave(fetchedPosts);

        // Assert (驗證) - 應該存 1 篇
        verify(rawPostRepository).saveAll(argThat(list ->
                ((List<?>) list).size() == 1
        ));
    }
}
