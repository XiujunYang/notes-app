package com.example.notesapp.controller;

import com.example.notesapp.dto.NoteDTO;
import com.example.notesapp.model.NoteSummary;
import com.example.notesapp.model.Tag;
import com.example.notesapp.service.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(value = "/api/v1/notes", produces = MediaType.APPLICATION_JSON_VALUE)
@io.swagger.v3.oas.annotations.tags.Tag(name = "Notes Management", description = "APIs for managing notes")
@Log4j2
public class NoteController {
    @Autowired
    private NoteService noteService;

    @Operation(
            summary = "Create a new note",
            description = "Create a new note with the provided content and tags",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Note created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "500", description = "Note created failed", content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @PostMapping
    public ResponseEntity<NoteDTO> createNote(
            @Parameter(description = "User ID of the note owner") @RequestParam String userId,
            @Valid @RequestBody NoteDTO noteDTO) {
        return ResponseEntity.ok(noteService.createNote(userId, noteDTO));
    }

    @Operation(
            summary = "Get notes and support tags filter and pagination",
            description = "Retrieve notes for a user, optionally filtered by tags",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Notes retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "500", description = "Query notes failed", content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @GetMapping
    public ResponseEntity<Page<NoteSummary>> getAllNotes(
            @Parameter(description = "User ID to retrieve notes for") @RequestParam String userId,
            @Parameter(description = "Optional tags to filter notes by") @RequestParam(required = false) Set<Tag> tags,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(noteService.getNotesByTags(userId, tags, page, size));
    }

    @Operation(
            summary = "Get note by ID",
            description = "Retrieve a specific note by its ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Note retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = "Note not found", content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "500", description = "Get note detail failed", content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<NoteDTO> getNoteById(
            @Parameter(description = "ID of the note to retrieve") @NotBlank @PathVariable String id) {
        return ResponseEntity.ok(noteService.getNoteById(id));
    }

    @Operation(
            summary = "Get note statistics",
            description = "Retrieve word statistics for a specific note's content in descending order",
            responses = {
                @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
                @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = String.class))),
                @ApiResponse(responseCode = "404", description = "Note not found", content = @Content(schema = @Schema(implementation = String.class))),
                @ApiResponse(responseCode = "500", description = "Get note statistics failed", content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String, Integer>> getStatsByNote(
            @Parameter(description = "ID of the note to get statistics for") @NotBlank @PathVariable String id) throws Exception {
        Map<String, Integer> stats = noteService.getNoteStats(id);
        return ResponseEntity.ok(stats);
    }

    @Operation(
            summary = "Update note",
            description = "Update an existing note with new content and/or tags",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Note updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = "Note not found", content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "500", description = "Update note failed", content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<NoteDTO> updateNote(
            @Parameter(description = "ID of the note to update") @NotBlank @PathVariable String id,
            @Valid @RequestBody NoteDTO noteDTO) {
        NoteDTO updatedNote = noteService.updateNote(id, noteDTO);
        return ResponseEntity.ok(updatedNote);
    }

    @Operation(
            summary = "Delete note",
            description = "Delete a note by its ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Note deleted successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = "Note not found", content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "500", description = "Update note failed", content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteNote(
            @Parameter(description = "ID of the note to delete") @NotBlank @PathVariable String id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get supported tags",
            description = "Retrieve all supported tags for notes",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tags retrieved successfully"),
                    @ApiResponse(responseCode = "500", description = "Update note failed", content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @GetMapping("/tags")
    public ResponseEntity<List<String>> getSupportedTags() {
        return ResponseEntity.ok(Arrays.stream(Tag.values()).map(Enum::name).toList());
    }
}
