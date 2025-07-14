package com.example.notesapp;

import com.example.notesapp.model.Note;
import com.example.notesapp.model.Tag;
import com.example.notesapp.repository.NoteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Import(MongoRepositoryTestConfig.class)
public class NoteControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.2");

    @Autowired
    private NoteRepository noteRepository;

    private static final String TEST_USER_ID = "test-user";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @DynamicPropertySource
    static void overrideMongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @AfterEach
    void tearDown() {
        noteRepository.deleteAll();
    }

    @Test
    void createNote_returns200_whenValidData() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/notes")
                .param("userId", TEST_USER_ID)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("{\"title\":\"Test Note\",\"content\":\"This is a test note\",\"tags\":[\"BUSINESS\"]}")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("title").value("Test Note"))
            .andExpect(jsonPath("content").value("This is a test note"))
            .andExpect(jsonPath("tags").isArray())
            .andReturn();

        String response = result.getResponse().getContentAsString();
        String noteId = JsonPath.read(response, "$.id");
        assertNotNull(noteId);
    }

    @ParameterizedTest
    @ValueSource(strings = {"title", "content", "userId"})
    void createNote_returns400_whenFieldMissing(String missedField) throws Exception {
        String requestBody;
        if ("title".equals(missedField)) {
            requestBody = "{\"content\":\"This is a test note\",\"tags\":[\"BUSINESS\"]}";
        } else if("content".equals(missedField)) {
            requestBody = "{\"title\":\"This is a title\",\"tags\":[\"BUSINESS\"]}";
        } else {
            requestBody = "{\"title\":\"This is a title\",\"content\":\"This is a test note\",\"tags\":[\"BUSINESS\"]}";
        }

        if("userId".equals(missedField)) {
            mockMvc.perform(post("/api/v1/notes").contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody))
                    .andExpect(status().isBadRequest()).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.blankString())));
        } else {
            mockMvc.perform(post("/api/v1/notes").param("userId", TEST_USER_ID)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.blankString())));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "CUSTOMIZED_TAG"})
    void createNote_returns400_withInvalidTag(String tags) throws Exception {
        String requestBody = "{\"title\":\"This is a title\",\"content\":\"This is a test note\",\"tags\":[\"" + tags + "\"]}";

        mockMvc.perform(post("/api/v1/notes").param("userId", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.blankString())));
    }

    @ParameterizedTest
    @CsvSource(value = {"userId1;;0;2;2;3;2", // query without tags
            "userId1;;1;2;1;3;2", // query next page
            "userId1;BUSINESS,PERSONAL;0;10;3;3;1",
            "userId1;BUSINESS;0;1;1;2;2",
            "userId2;BUSINESS;0;5;1;1;1"}, delimiter = ';')
    void getAllNotes_returns200_whenValidUserIdAndTags(String queryUserId, String queryTags, String queryPage, String querySize,
                                                       int expectedContentSize, int expectedTotalElements, int expectedTotalPage) throws Exception {
        generatedNoteInDB("userId1", Set.of(Tag.BUSINESS));
        generatedNoteInDB("userId1", Set.of(Tag.BUSINESS, Tag.PERSONAL));
        generatedNoteInDB("userId1", Set.of(Tag.IMPORTANT, Tag.PERSONAL));
        generatedNoteInDB("userId2", Set.of(Tag.BUSINESS, Tag.IMPORTANT));

        mockMvc.perform(get("/api/v1/notes")
                        .param("userId", queryUserId)
                .param("tags", queryTags)
                .param("page", queryPage)
                .param("size", querySize)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("content").isArray()).andExpect(jsonPath("content", hasSize(expectedContentSize)))
                .andExpect(jsonPath("pageable.pageNumber").value(queryPage))
                .andExpect(jsonPath("pageable.pageSize").value(querySize))
                .andExpect(jsonPath("numberOfElements").value(expectedContentSize))
                .andExpect(jsonPath("totalElements").value(expectedTotalElements))
                .andExpect(jsonPath("totalPages").value(expectedTotalPage));
    }

    @Test
    void getAllNotes_return400_whenMissingUserId() throws Exception {
        mockMvc.perform(get("/api/v1/notes").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.blankString())));
    }

    @Test
    void getGetStatsByNote_return200_whenNoteIsExisted() throws Exception {
        String noteId = generatedNoteInDB(Set.of(Tag.BUSINESS));
        mockMvc.perform(get("/api/v1/notes/{id}", noteId).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk()).andExpect(jsonPath("id").isNotEmpty())
                .andExpect(jsonPath("title").isNotEmpty())
                .andExpect(jsonPath("content").isNotEmpty()).andExpect(jsonPath("tags").isArray());
    }

    @Test
    void getGetStatsByNote_return404_whenNoteIsUnExisted() throws Exception {
        mockMvc.perform(get("/api/v1/notes/{id}", "notExistedNoteId").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(content().string("NoteId notExistedNoteId not existed"));
    }

    @Test
    void updateNote_returns200_whenValidId() throws Exception {
        String noteId = generatedNoteInDB();

        mockMvc.perform(put("/api/v1/notes/{id}", noteId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("{\"title\":\"Updated Note\",\"content\":\"This is a new content\",\"tags\":[\"IMPORTANT\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(noteId))
                .andExpect(jsonPath("title").value("Updated Note"))
                .andExpect(jsonPath("content").value("This is a new content"))
                .andExpect(jsonPath("tags").isArray()).andExpect(jsonPath("tags[0]").value("IMPORTANT"));
    }

    @Test
    void updateNote_return404_whenNoteIsNotExisted() throws Exception {
        mockMvc.perform(put("/api/v1/notes/{id}", "notExistedNoteId")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"title\":\"Updated Note\",\"content\":\"This is a new content\",\"tags\":[\"IMPORTANT\"]}")
                )
                .andExpect(status().isNotFound())
                .andExpect(content().string("NoteId notExistedNoteId not existed"));
    }

    @Test
    void updateNote_return409_whenOptimisticConcurrencyOccur() throws Exception { // verify optimistic locking for mongodb
        String noteId = generatedNoteInDB();
        mockMvc.perform(put("/api/v1/notes/{id}", noteId)
                        .param("userId", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"title\":\"OptimisticLockingFailed\",\"content\":\"This is a test note\",\"tags\":[\"BUSINESS\"]}")
                )
                .andExpect(status().isConflict()).andExpect(content().string("there is data conflict, please try it again"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void deleteNote_returns204_whenNotMatterIfNoteIsExisted(boolean isNoteExisted) throws Exception {
        String noteId;
        if(isNoteExisted) {
            noteId = generatedNoteInDB();
        } else {
            noteId = "notExistedNoteId";
        }
        mockMvc.perform(delete("/api/v1/notes/{id}", noteId))
            .andExpect(status().isNoContent());
    }

    @Test
    void getStatsByNote_return200_whenNoteIsValid() throws Exception {
        String noteId = generatedNoteInDB(TEST_USER_ID, "In common, Stats that used in there is a most common of stats way, it is sorted by common way. ", Set.of(Tag.IMPORTANT));
        MvcResult result = mockMvc.perform(get("/api/v1/notes/{id}/stats", noteId).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(6)).andReturn();
        assertEquals(result.getResponse().getContentAsString(), "{\"common\":3,\"stats\":2,\"way\":2,\"most\":1,\"sorted\":1,\"used\":1}");
    }

    @Test
    void getStatsByNote_return404_whenNoteIsNotExisted() throws Exception {
        mockMvc.perform(get("/api/v1/notes/{id}/stats", "notExistedNoteId").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound()).andExpect(content().string("NoteId notExistedNoteId not existed"));
    }

    @Test
    void getTags_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/notes/tags").contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0]").value("BUSINESS"))
            .andExpect(jsonPath("$[1]").value("PERSONAL"))
            .andExpect(jsonPath("$[2]").value("IMPORTANT"));
    }

    @Test
    void verifyCreatedAtAndUpdatedAtBehaviorInMongoDBAudit() throws Exception {
        String noteId = generatedNoteInDB();
        Optional<Note> savedNote = noteRepository.findById(noteId);

        mockMvc.perform(put("/api/v1/notes/{id}", noteId)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"title\":\"Updated Note\",\"content\":\"This is a new content\",\"tags\":[\"IMPORTANT\"]}"))
                .andExpect(status().isOk());
        Optional<Note> updatedNote = noteRepository.findById(noteId);
        assertTrue(savedNote.isPresent());
        assertTrue(updatedNote.isPresent());
        assertEquals(savedNote.get().getCreatedAt(), updatedNote.get().getCreatedAt());
        assertTrue(updatedNote.get().getUpdatedAt().isAfter(savedNote.get().getUpdatedAt()));
    }

    /**
     * return noteId
     */
    private String generatedNoteInDB(String userId, String content, Set<Tag> tags) {
        try {
            String tagsJsonStr = objectMapper.writeValueAsString(tags);
            MvcResult result = mockMvc.perform(post("/api/v1/notes")
                            .param("userId", userId)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content("{\"title\":\"Updated Note\",\"content\":\"" + content + "\",\"tags\":" + tagsJsonStr + "}")
                    )
                    .andExpect(status().isOk()).andReturn();
            String response = result.getResponse().getContentAsString();
            return JsonPath.read(response, "$.id");
        } catch (Exception e) {
            fail("setup pre-test data failed.");
        }
        return null;
    }

    private String generatedNoteInDB(String userId, Set<Tag> tags) {
        return generatedNoteInDB(userId, "This is a test note", tags);
    }

    private String generatedNoteInDB(Set<Tag> tags) {
        return generatedNoteInDB(TEST_USER_ID, "This is a test note", tags);
    }

    private String generatedNoteInDB() {
        return generatedNoteInDB(TEST_USER_ID, "This is a test note", Set.of());
    }
}
