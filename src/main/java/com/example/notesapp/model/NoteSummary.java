package com.example.notesapp.model;

import java.time.Instant;
import java.util.Set;

public interface NoteSummary {
    String getId();
    String getTitle();
    Set<Tag> getTags();
    Instant getCreatedAt();
    Instant getUpdatedAt();
}
