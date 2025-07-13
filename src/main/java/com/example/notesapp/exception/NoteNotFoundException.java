package com.example.notesapp.exception;

public class NoteNotFoundException extends RuntimeException {
    private String noteId;
    public NoteNotFoundException(String noteId) {
        super("NoteId " + noteId + " not existed");
    }
}
