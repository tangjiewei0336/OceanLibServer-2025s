package com.oriole.ocean.service.impl;

import com.oriole.ocean.common.po.mongo.AnswerEntity;
import com.oriole.ocean.common.po.mongo.QuestionEntity;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.repository.AnswerRepository;
import com.oriole.ocean.service.QuestionService;
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
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnswerServiceImplTest {

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private QuestionService questionService;

    @InjectMocks
    private AnswerServiceImpl answerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void submitAnswer_Success() {
        // Arrange
        Integer questionId = 1;
        String content = "Test answer content";
        String userId = "user123";
        AnswerEntity savedAnswer = new AnswerEntity();
        savedAnswer.setId(1);
        when(answerRepository.save(any(AnswerEntity.class))).thenReturn(savedAnswer);

        // Act
        MsgEntity<Integer> result = answerService.submitAnswer(questionId, content, userId);

        // Assert
        assertNotNull(result);
        assertEquals("SUCCESS", result.getCode());
        assertEquals(1, result.getData());
        verify(answerRepository, times(1)).save(any(AnswerEntity.class));
    }

    @Test
    void getAnswersByQuestionId_Success() {
        // Arrange
        Integer questionId = 1;
        int page = 1;
        int pageSize = 10;
        List<AnswerEntity> answers = Arrays.asList(new AnswerEntity(), new AnswerEntity());
        Page<AnswerEntity> answerPage = new PageImpl<>(answers);
        when(answerRepository.findByQuestionIdAndIsDeletedFalse(eq(questionId), any(Pageable.class)))
                .thenReturn(answerPage);

        // Act
        MsgEntity<Page<AnswerEntity>> result = answerService.getAnswersByQuestionId(questionId, page, pageSize);

        // Assert
        assertNotNull(result);
        assertEquals("SUCCESS", result.getCode());
        assertEquals(2, result.getData().getTotalElements());
        verify(answerRepository, times(1)).findByQuestionIdAndIsDeletedFalse(eq(questionId), any(Pageable.class));
    }

    @Test
    void updateAnswer_Success() {
        // Arrange
        Integer answerId = 1;
        String content = "Updated answer content";
        String userId = "user123";
        AnswerEntity existingAnswer = new AnswerEntity();
        existingAnswer.setId(answerId);
        existingAnswer.setUserId(userId);
        when(answerRepository.findByIdAndIsDeletedFalse(answerId)).thenReturn(existingAnswer);
        when(answerRepository.save(any(AnswerEntity.class))).thenReturn(existingAnswer);

        // Act
        MsgEntity<AnswerEntity> result = answerService.updateAnswer(answerId, content, userId);

        // Assert
        assertNotNull(result);
        assertEquals("SUCCESS", result.getCode());
        verify(answerRepository, times(1)).save(any(AnswerEntity.class));
    }

    @Test
    void updateAnswer_NotFound() {
        // Arrange
        Integer answerId = 1;
        String content = "Updated answer content";
        String userId = "user123";
        when(answerRepository.findByIdAndIsDeletedFalse(answerId)).thenReturn(null);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> 
            answerService.updateAnswer(answerId, content, userId)
        );
    }

    @Test
    void updateAnswer_Unauthorized() {
        // Arrange
        Integer answerId = 1;
        String content = "Updated answer content";
        String userId = "user123";
        AnswerEntity existingAnswer = new AnswerEntity();
        existingAnswer.setId(answerId);
        existingAnswer.setUserId("differentUser");
        when(answerRepository.findByIdAndIsDeletedFalse(answerId)).thenReturn(existingAnswer);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> 
            answerService.updateAnswer(answerId, content, userId)
        );
    }

    @Test
    void deleteAnswer_Success() {
        // Arrange
        Integer answerId = 1;
        String userId = "user123";
        AnswerEntity existingAnswer = new AnswerEntity();
        existingAnswer.setId(answerId);
        existingAnswer.setUserId(userId);
        when(answerRepository.findByIdAndIsDeletedFalse(answerId)).thenReturn(existingAnswer);

        // Act
        MsgEntity<String> result = answerService.deleteAnswer(answerId, userId);

        // Assert
        assertNotNull(result);
        assertEquals("SUCCESS", result.getCode());
        verify(answerRepository, times(1)).save(any(AnswerEntity.class));
    }

    @Test
    void getAnswersByUserId_Success() {
        // Arrange
        String username = "user123";
        Integer page = 1;
        Integer pageSize = 10;
        List<AnswerEntity> answers = Arrays.asList(new AnswerEntity(), new AnswerEntity());
        Page<AnswerEntity> answerPage = new PageImpl<>(answers);
        when(answerRepository.findByUserIdAndIsDeletedFalse(eq(username), any(Pageable.class)))
                .thenReturn(answerPage);
        when(questionService.getQuestionById(any())).thenReturn(new QuestionEntity());

        // Act
        MsgEntity<Page<AnswerEntity>> result = answerService.getAnswersByUserId(username, page, pageSize);

        // Assert
        assertNotNull(result);
        assertEquals("SUCCESS", result.getCode());
        assertEquals(2, result.getData().getTotalElements());
        verify(answerRepository, times(1)).findByUserIdAndIsDeletedFalse(eq(username), any(Pageable.class));
    }

    @Test
    void getAllAnswers_Success() {
        // Arrange
        Integer page = 1;
        Integer pageSize = 10;
        List<AnswerEntity> answers = Arrays.asList(new AnswerEntity(), new AnswerEntity());
        Page<AnswerEntity> answerPage = new PageImpl<>(answers);
        when(answerRepository.findByIsDeletedFalse(any(Pageable.class))).thenReturn(answerPage);
        when(questionService.getQuestionById(any())).thenReturn(new QuestionEntity());

        // Act
        MsgEntity<Page<AnswerEntity>> result = answerService.getAllAnswers(page, pageSize);

        // Assert
        assertNotNull(result);
        assertEquals("SUCCESS", result.getCode());
        assertEquals(2, result.getData().getTotalElements());
        verify(answerRepository, times(1)).findByIsDeletedFalse(any(Pageable.class));
    }
} 