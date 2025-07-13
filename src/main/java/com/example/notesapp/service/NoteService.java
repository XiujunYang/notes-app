package com.example.notesapp.service;

import com.example.notesapp.dto.NoteDTO;
import com.example.notesapp.exception.NoteNotFoundException;
import com.example.notesapp.model.Note;
import com.example.notesapp.model.NoteSummary;
import com.example.notesapp.model.Tag;
import com.example.notesapp.repository.NoteRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class NoteService {
    @Autowired
    private NoteRepository noteRepository;
    
    @Autowired
    private ModelMapper modelMapper;

    public NoteDTO createNote(String userId, NoteDTO noteDTO) {
        log.debug("Creating note with userId: {}", userId);
        log.debug("NoteDTO received: {}", noteDTO);
        
        Note note = modelMapper.map(noteDTO, Note.class);
        note.setOwnerId(userId);
        Note savedNote = noteRepository.save(note);
        log.debug("Saved Note: {}", savedNote);
        return modelMapper.map(savedNote, NoteDTO.class);
    }

    public Page<NoteSummary> getNotesByTags(String userId, Set<Tag> tags, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.asc("title")));
        Page<NoteSummary> notePage;
        log.debug("getNotesByTags {} ownerId {} page {} pageSize {}", tags, userId, page, pageSize);
        if (ObjectUtils.isEmpty(tags)) {
            notePage = noteRepository.findByOwnerId(userId, pageable);
        } else {
            notePage = noteRepository.findByOwnerIdAndTagsIn(userId, tags, pageable);
        }
        log.info("getNotesByTags totalElements: {} totalPage: {}", notePage.getTotalElements(), notePage.getTotalPages());
        List<NoteSummary> content = notePage.getContent()
                .stream()
                .map(note -> modelMapper.map(note, NoteSummary.class))
                .toList();
        return new PageImpl<>(content, pageable, notePage.getTotalElements());
    }

    public NoteDTO getNoteById(String id) {
        return noteRepository.findById(id).map(note -> modelMapper.map(note, NoteDTO.class)).orElseThrow(() -> new NoteNotFoundException(id));
    }

    public NoteDTO updateNote(String id, NoteDTO noteDTO) {
        Note savedNote = noteRepository.findById(id).map(note -> {
            note.setTitle(noteDTO.getTitle());
            note.setContent(noteDTO.getContent());
            if(!ObjectUtils.isEmpty(noteDTO.getTags())) {
                note.setTags(noteDTO.getTags());
            }
            return note;
        }).orElseThrow(() -> new NoteNotFoundException(id));
        noteRepository.save(savedNote);
        return modelMapper.map(savedNote, NoteDTO.class);
    }

    public void deleteNote(String id) {
        noteRepository.deleteById(id);
    }

    public Map<String, Integer> getNoteStats(String noteId) throws Exception {
        Note note = noteRepository.findById(noteId).orElseThrow(() -> new NoteNotFoundException(noteId));
        if(ObjectUtils.isEmpty(note.getContent())) {
            return new LinkedHashMap<>();
        }
        byte[] contentBytes = note.getContent().getBytes();
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(new ByteArrayInputStream(contentBytes), StandardCharsets.UTF_8);
            return countWords(new BufferedReader(reader));
        } catch (Exception e) {
            log.error("getNoteStats for noteId {} error:", noteId, e);
            throw e;
        } finally {
            if(reader != null ) {
                reader.close();
            }
        }
    }

    private Map<String, Integer> countWords(Reader textReader) throws Exception {
        CharArraySet stopWords = EnglishAnalyzer.ENGLISH_STOP_WORDS_SET;
        Analyzer analyzer = new StandardAnalyzer(stopWords);

        Map<String, Long> freq = new HashMap<>();
        TokenStream ts = analyzer.tokenStream("field", textReader); // default: case insensitive
        try {
            CharTermAttribute termAttr = ts.addAttribute(CharTermAttribute.class);
            ts.reset();

            while (ts.incrementToken()) {
                String term = termAttr.toString();
                freq.merge(term, 1L, Long::sum);
            }
        } finally {
            ts.end();
            analyzer.close();
        }
        return freq.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().intValue(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }
}