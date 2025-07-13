package com.example.notesapp.dto;

import com.example.notesapp.model.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.EnumSet;
import java.util.Set;

@Data
public class NoteDTO {
    private String id;
    @NotBlank
    private String title;
    @NotBlank
    private String content;
    private Set<Tag> tags = EnumSet.noneOf(Tag.class);
}
