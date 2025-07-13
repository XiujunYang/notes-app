package com.example.notesapp.repository;

import com.example.notesapp.model.Note;
import com.example.notesapp.model.NoteSummary;
import com.example.notesapp.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface NoteRepository extends MongoRepository<Note, String> {
    Page<NoteSummary> findByOwnerId(String ownerId, Pageable pageable);
    Page<NoteSummary> findByOwnerIdAndTagsIn(String ownerId, Collection<Tag> tags, Pageable pageable);
}
