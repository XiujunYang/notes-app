package com.example.notesapp.util;

import com.example.notesapp.dto.NoteDTO;
import com.example.notesapp.model.Note;
import com.example.notesapp.model.Tag;

import java.time.Instant;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;
import java.util.stream.Collectors;

public class TestDataGenerator {
    public static NoteDTO createTestNoteDTO() {
        NoteDTO noteDTO = new NoteDTO();
        noteDTO.setTitle("Test Note");
        noteDTO.setContent("This is a test note");
        noteDTO.setTags(EnumSet.of(Tag.BUSINESS));
        return noteDTO;
    }

    public static NoteDTO createTestNoteDTOWithTags(Tag... tags) {
        NoteDTO noteDTO = new NoteDTO();
        noteDTO.setTitle("Test Note with Tags");
        noteDTO.setContent("This is a test note with multiple tags");
        noteDTO.setTags(Arrays.stream(tags).collect(Collectors.toSet()));
        return noteDTO;
    }

    public static NoteDTO createInvalidNoteDTO() {
        NoteDTO noteDTO = new NoteDTO();
        noteDTO.setContent("This note is invalid because it has no title");
        return noteDTO;
    }

    public static Note createNoteWithTags(String content) {
        String randomUUID = UUID.randomUUID().toString();
        Instant now = Instant.now();
        return Note.builder().id(randomUUID).ownerId("testedUser").title("title").content(content).createdAt(now).updatedAt(now).version(0L).build();
    }
}
