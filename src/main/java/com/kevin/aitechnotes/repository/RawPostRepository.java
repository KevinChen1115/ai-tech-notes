package com.kevin.aitechnotes.repository;

import com.kevin.aitechnotes.entity.RawPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

// JpaRepository 已經內建了 save、findAll、findById 等常用方法
// 我們不需要自己寫 SQL，Spring Data JPA 會自動處理
public interface RawPostRepository extends JpaRepository<RawPost, UUID> {
    List<RawPost> findByIsProcessed(boolean isProcessed);
}