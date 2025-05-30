package com.oriole.ocean.service.impl;

import com.oriole.ocean.common.po.mongo.QuestionEntity;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.repository.MongoQuestionRepository;
import com.oriole.ocean.service.SequenceGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class QuestionServiceImplTest {

    @Mock
    private MongoQuestionRepository mongoQuestionRepository;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    @InjectMocks
    private QuestionServiceImpl questionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createQuestion_Success() {
        // Arrange
        String title = "Test Question";
        String content = "Test Content";
        String userId = "user123";
        when(sequenceGeneratorService.getNextSequence("questions")).thenReturn(1);
        QuestionEntity savedQuestion = new QuestionEntity();
        savedQuestion.setBindId(1);
        when(mongoQuestionRepository.save(any(QuestionEntity.class))).thenReturn(savedQuestion);

        // Act
        MsgEntity<Integer> result = questionService.createQuestion(title, content, userId);

        // Assert
        assertNotNull(result);
        assertEquals("SUCCESS", result.getCode());
        assertEquals(1, result.getData());
        verify(mongoQuestionRepository, times(1)).save(any(QuestionEntity.class));
    }

    @Test
    void getQuestions_Success() {
        // Arrange
        int page = 1;
        int pageSize = 10;
        String username = "user123";
        Integer sortMethod = 0;
        Boolean includeDeleted = false;
        List<QuestionEntity> questions = Arrays.asList(new QuestionEntity(), new QuestionEntity());
        Page<QuestionEntity> questionPage = new PageImpl<>(questions);
        when(mongoQuestionRepository.findByIsDeletedFalse(any(Pageable.class))).thenReturn(questionPage);

        // Act
        MsgEntity<Page<QuestionEntity>> result = questionService.getQuestions(page, pageSize, username, sortMethod, includeDeleted);

        // Assert
        assertNotNull(result);
        assertEquals("SUCCESS", result.getCode());
        assertEquals(2, result.getData().getTotalElements());
        verify(mongoQuestionRepository, times(1)).findByIsDeletedFalse(any(Pageable.class));
    }

    @Test
    void getQuestionById_Success() {
        // Arrange
        Integer questionId = 1;
        QuestionEntity question = new QuestionEntity();
        question.setBindId(questionId);
        when(mongoQuestionRepository.findByBindIdAndIsDeletedFalse(questionId)).thenReturn(question);

        // Act
        QuestionEntity result = questionService.getQuestionById(questionId);

        // Assert
        assertNotNull(result);
        assertEquals(questionId, result.getBindId());
        verify(mongoQuestionRepository, times(1)).findByBindIdAndIsDeletedFalse(questionId);
    }

    @Test
    void getQuestionById_NotFound() {
        // Arrange
        Integer questionId = 1;
        when(mongoQuestionRepository.findByBindIdAndIsDeletedFalse(questionId)).thenReturn(null);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> 
            questionService.getQuestionById(questionId)
        );
    }

    @Test
    void getQuestionByIds_Success() {
        // Arrange
        List<Integer> questionIds = Arrays.asList(1, 2);
        List<QuestionEntity> questions = Arrays.asList(
            new QuestionEntity(), new QuestionEntity()
        );
        when(mongoQuestionRepository.findByBindIdInAndIsDeletedFalse(questionIds)).thenReturn(questions);

        // Act
        List<QuestionEntity> result = questionService.getQuestionByIds(questionIds);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(mongoQuestionRepository, times(1)).findByBindIdInAndIsDeletedFalse(questionIds);
    }

    @Test
    void getQuestionByIds_EmptyList() {
        // Arrange
        List<Integer> questionIds = Collections.emptyList();

        // Act
        List<QuestionEntity> result = questionService.getQuestionByIds(questionIds);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mongoQuestionRepository, never()).findByBindIdInAndIsDeletedFalse(any());
    }

    @Test
    void updateQuestion_Success() {
        // Arrange
        Integer questionId = 1;
        String title = "Updated Title";
        String content = "Updated Content";
        String userId = "user123";
        QuestionEntity existingQuestion = new QuestionEntity();
        existingQuestion.setBindId(questionId);
        existingQuestion.setUserId(userId);
        when(mongoQuestionRepository.findByBindIdAndIsDeletedFalse(questionId)).thenReturn(existingQuestion);
        when(mongoQuestionRepository.save(any(QuestionEntity.class))).thenReturn(existingQuestion);

        // Act
        MsgEntity<QuestionEntity> result = questionService.updateQuestion(
            questionId, title, content, true, false, null, userId
        );

        // Assert
        assertNotNull(result);
        assertEquals("SUCCESS", result.getCode());
        verify(mongoQuestionRepository, times(1)).save(any(QuestionEntity.class));
    }

    @Test
    void updateQuestion_NotFound() {
        // Arrange
        Integer questionId = 1;
        String title = "Updated Title";
        String content = "Updated Content";
        String userId = "user123";
        when(mongoQuestionRepository.findByBindIdAndIsDeletedFalse(questionId)).thenReturn(null);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> 
            questionService.updateQuestion(questionId, title, content, true, false, null, userId)
        );
    }

    @Test
    void updateQuestion_Unauthorized() {
        // Arrange
        Integer questionId = 1;
        String title = "Updated Title";
        String content = "Updated Content";
        String userId = "user123";
        QuestionEntity existingQuestion = new QuestionEntity();
        existingQuestion.setBindId(questionId);
        existingQuestion.setUserId("differentUser");
        when(mongoQuestionRepository.findByBindIdAndIsDeletedFalse(questionId)).thenReturn(existingQuestion);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> 
            questionService.updateQuestion(questionId, title, content, true, false, null, userId)
        );
    }

    @Test
    void deleteQuestion_Success() {
        // Arrange
        Integer questionId = 1;
        String userId = "user123";
        QuestionEntity existingQuestion = new QuestionEntity();
        existingQuestion.setBindId(questionId);
        existingQuestion.setUserId(userId);
        when(mongoQuestionRepository.findByBindIdAndIsDeletedFalse(questionId)).thenReturn(existingQuestion);

        // Act
        MsgEntity<String> result = questionService.deleteQuestion(questionId, userId);

        // Assert
        assertNotNull(result);
        assertEquals("SUCCESS", result.getCode());
        verify(mongoQuestionRepository, times(1)).save(any(QuestionEntity.class));
    }

    @Test
    void deleteQuestion_NotFound() {
        // Arrange
        Integer questionId = 1;
        String userId = "user123";
        when(mongoQuestionRepository.findByBindIdAndIsDeletedFalse(questionId)).thenReturn(null);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> 
            questionService.deleteQuestion(questionId, userId)
        );
    }

    @Test
    void deleteQuestion_Unauthorized() {
        // Arrange
        Integer questionId = 1;
        String userId = "user123";
        QuestionEntity existingQuestion = new QuestionEntity();
        existingQuestion.setBindId(questionId);
        existingQuestion.setUserId("differentUser");
        when(mongoQuestionRepository.findByBindIdAndIsDeletedFalse(questionId)).thenReturn(existingQuestion);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> 
            questionService.deleteQuestion(questionId, userId)
        );
    }
} 