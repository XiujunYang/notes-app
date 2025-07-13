package com.example.notesapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notes")
@CompoundIndexes({
        @CompoundIndex(name = "ownerId_updatedAt_title_idx", def = "{'ownerId':1, 'updatedAt':-1, 'title':1}"),
        @CompoundIndex(name = "ownerId_tag_updatedAt_title_idx", def = "{'ownerId':1, 'tags':1, 'updatedAt':-1, 'title':1}")
})
public class Note {
    @Id
    private String id;
    @Indexed
    private String ownerId;
    @Indexed
    private String title;
    private String content;
    @Default
    private Set<Tag> tags = EnumSet.noneOf(Tag.class);
    @CreatedDate
    private Instant createdAt;
    @Indexed(direction = IndexDirection.DESCENDING)
    @LastModifiedDate
    private Instant updatedAt;
    @Version
    private Long version; // avoid: lost update or dirty Read
}
