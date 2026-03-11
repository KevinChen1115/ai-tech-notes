package com.kevin.aitechnotes.repository;

import com.kevin.aitechnotes.entity.AiNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AiNoteRepository extends JpaRepository<AiNote, UUID> {
    List<AiNote> findAllByOrderByCreatedAtDesc();
}
