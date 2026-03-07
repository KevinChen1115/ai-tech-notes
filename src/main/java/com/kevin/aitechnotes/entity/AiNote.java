package com.kevin.aitechnotes.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "ai_notes")
public class AiNote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "post_id")
    private RawPost post;

    private Boolean isValuable;

    private String tags;

    @Column(columnDefinition = "TEXT")
    private String summary;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist(){
        createdAt = LocalDateTime.now();
    }
}
