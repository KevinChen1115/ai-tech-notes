package com.kevin.aitechnotes.repository;

import com.kevin.aitechnotes.entity.AiNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AiNoteRepository extends JpaRepository<AiNote, UUID> {
    @Query("SELECT n FROM AiNote n JOIN FETCH n.post ORDER BY n.createdAt DESC")
    List<AiNote> findAllWithPostOrderByCreatedAtDesc();
}
