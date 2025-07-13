package com.example.notesapp.service;

import com.example.notesapp.repository.NoteRepository;
import com.example.notesapp.util.TestDataGenerator;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {
    @InjectMocks
    private NoteService noteService;
    @Mock
    private NoteRepository noteRepository;
    @Mock
    private ModelMapper modelMapper;

    static Stream<Arguments> noteStatsProvider() {
        return Stream.of(
                Arguments.of(
                        "This is a test assessment, and Assessment is used to Test candidate.",
                        new LinkedHashMap<String, Integer>() {{
                                put("assessment", 2);
                                put("test", 2);
                                put("candidate", 1);
                                put("used", 1);
                        }}
                ),
                Arguments.of(
                        "abc des fdesdsfdsf     fwerdsiewd, fwerdsiEWd",
                        new LinkedHashMap<String, Integer>() {{
                            put("fwerdsiewd", 2);
                            put("abc", 1);
                            put("des", 1);
                            put("fdesdsfdsf", 1);
                        }}
                ),
                Arguments.of(
                        "", new LinkedHashMap<String, Integer>()
                )
        );
    }

    @ParameterizedTest
    @MethodSource("noteStatsProvider")
    void getNoteStats(String noteContent, Map<String, Integer> expected) {
        when(noteRepository.findById(anyString())).thenReturn(Optional.of(TestDataGenerator.createNoteWithTags(noteContent)));
        try {
            Map<String, Integer> result = noteService.getNoteStats("noteId");
            assertNotNull(result);
            assertEquals(expected.size(), result.size());
            assertEquals(expected, result, "The result should match the expected map in both order, keys and values");
        } catch (Exception e) {
            fail();
        }
    }
}