package com.kevin.aitechnotes.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data           // Lombok 自動產生 getter/setter
@Entity         // 告訴 JPA 這個 class 對應資料庫的一張表
@Table(name = "raw_posts")  // 對應的表格名稱
public class RawPost {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String platform;
    private String author;

    @Column(columnDefinition = "TEXT")  // 指定欄位類型為 TEXT，不限長度
    private String content;

    private String url;
    private LocalDateTime scrapedAt;
    private Boolean isProcessed = false;
}