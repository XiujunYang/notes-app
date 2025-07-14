package com.example.notesapp;

import com.example.notesapp.model.Note;
import com.example.notesapp.model.NoteSummary;
import com.example.notesapp.model.Tag;
import com.example.notesapp.repository.NoteRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@TestConfiguration
public class MongoRepositoryTestConfig {

    @Bean
    @Primary
    public NoteRepository mockNoteRepository(NoteRepository realRepo) {
        return new NoteRepository() {
            @Override
            public List<Note> findAll(Sort sort) {
                return List.of();
            }

            @Override
            public Page<Note> findAll(Pageable pageable) {
                return null;
            }

            @Override
            public <S extends Note> S save(S entity) {
                if ("OptimisticLockingFailed".equals(entity.getTitle())) { // tested case in NoteControllerIntegrationTest
                    throw new OptimisticLockingFailureException("OptimisticLockingFailed");
                }
                return realRepo.save(entity);
            }

            @Override
            public <S extends Note> S insert(S entity) {
                return realRepo.insert(entity);
            }

            @Override
            public <S extends Note> List<S> insert(Iterable<S> entities) {
                return realRepo.insert(entities);
            }

            @Override
            public <S extends Note> Optional<S> findOne(Example<S> example) {
                return realRepo.findOne(example);
            }

            @Override
            public <S extends Note> List<S> findAll(Example<S> example) {
                return realRepo.findAll(example);
            }

            @Override
            public <S extends Note> List<S> findAll(Example<S> example, Sort sort) {
                return realRepo.findAll(example, sort);
            }

            @Override
            public <S extends Note> Page<S> findAll(Example<S> example, Pageable pageable) {
                return realRepo.findAll(example, pageable);
            }

            @Override
            public <S extends Note> long count(Example<S> example) {
                return realRepo.count(example);
            }

            @Override
            public <S extends Note> boolean exists(Example<S> example) {
                return realRepo.exists(example);
            }

            @Override
            public <S extends Note, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
                return realRepo.findBy(example, queryFunction);
            }

            @Override
            public void delete(Note entity) {
                realRepo.delete(entity);
            }

            @Override
            public void deleteAllById(Iterable<? extends String> strings) {
                realRepo.deleteAllById(strings);
            }

            @Override
            public void deleteAll(Iterable<? extends Note> entities) {
                realRepo.deleteAll(entities);
            }

            @Override
            public void deleteAll() {
                realRepo.deleteAll();
            }

            @Override
            public <S extends Note> List<S> saveAll(Iterable<S> entities) {
                return realRepo.saveAll(entities);
            }

            @Override
            public Optional<Note> findById(String id) {
                return realRepo.findById(id);
            }

            @Override
            public boolean existsById(String id) {
                return realRepo.existsById(id);
            }

            @Override
            public List<Note> findAll() {
                return realRepo.findAll();
            }

            @Override
            public List<Note> findAllById(Iterable<String> ids) {
                return realRepo.findAllById(ids);
            }

            @Override
            public long count() {
                return realRepo.count();
            }

            @Override
            public void deleteById(String id) {
                realRepo.deleteById(id);
            }

            @Override
            public Page<NoteSummary> findByOwnerId(String ownerId, Pageable pageable) {
                return realRepo.findByOwnerId(ownerId, pageable);
            }

            @Override
            public Page<NoteSummary> findByOwnerIdAndTagsIn(String ownerId, Collection<Tag> tags, Pageable pageable) {
                return realRepo.findByOwnerIdAndTagsIn(ownerId, tags, pageable);
            }
        };
    }
}
