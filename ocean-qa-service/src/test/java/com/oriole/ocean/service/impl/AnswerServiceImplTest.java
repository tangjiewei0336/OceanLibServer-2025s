package com.oriole.ocean.service.impl;

import com.oriole.ocean.common.po.mongo.AnswerEntity;
import com.oriole.ocean.common.po.mongo.QuestionEntity;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.repository.AnswerRepository;
import com.oriole.ocean.service.QuestionService;
import com.oriole.ocean.service.SequenceGeneratorService;
import com.oriole.ocean.common.service.UserBehaviorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnswerServiceImplTest {

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private QuestionService questionService;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    private AnswerServiceImpl answerService;

    @BeforeEach
    void setUp() {
        answerService = new AnswerServiceImpl(answerRepository, questionService, sequenceGeneratorService);
    }

    @Test
    void submitAnswer_Success() {
        // Arrange
        Integer questionId = 1;
        String content = "Test answer content";
        String userId = "user123";
        
        // Mock question
        QuestionEntity question = new QuestionEntity();
        question.setBindId(questionId);
        question.setIsPosted(true);
        question.setIsDeleted(false);
        question.setIsHidden(false);
        when(questionService.getQuestionById(questionId)).thenReturn(question);
        
        // Mock sequence generator
        when(sequenceGeneratorService.getNextSequence("answer")).thenReturn(1);
        
        // Mock answer save
        AnswerEntity savedAnswer = new AnswerEntity();
        savedAnswer.setId(1);
        when(answerRepository.save(any(AnswerEntity.class))).thenReturn(savedAnswer);

        // Act
        MsgEntity<Integer> result = answerService.submitAnswer(questionId, content, userId);

        // Assert
        assertNotNull(result);
        assertEquals("Answer submitted successfully", result.getCode());
        assertEquals(1, result.getMsg());
        verify(questionService, times(1)).getQuestionById(questionId);
        verify(sequenceGeneratorService, times(1)).getNextSequence("answer");
        verify(answerRepository, times(1)).save(any(AnswerEntity.class));
    }

    @Test
    void submitAnswer_QuestionNotVisible() {
        // Arrange
        Integer questionId = 1;
        String content = "Test answer content";
        String userId = "user123";
        
        // Mock question that is not visible
        QuestionEntity question = new QuestionEntity();
        question.setBindId(questionId);
        question.setIsPosted(false);  // Question is not posted
        question.setIsDeleted(false);
        question.setIsHidden(false);
        when(questionService.getQuestionById(questionId)).thenReturn(question);

        // Act
        MsgEntity<Integer> result = answerService.submitAnswer(questionId, content, userId);

        // Assert
        assertNotNull(result);
        assertEquals("FAIL", result.getState());
        assertEquals("Question not visible", result.getCode());
        assertEquals(-1, result.getMsg());
        verify(questionService, times(1)).getQuestionById(questionId);
        verify(answerRepository, never()).save(any(AnswerEntity.class));
    }

    @Test
    void getAnswersByQuestionId_Success() {
        // Arrange
        Integer questionId = 1;
        int page = 1;
        int pageSize = 10;
        List<AnswerEntity> answers = Arrays.asList(new AnswerEntity(), new AnswerEntity());
        Page<AnswerEntity> answerPage = new PageImpl<>(answers);
        when(answerRepository.findByQuestionIdAndIsDeletedFalseAndQuestionVisibleTrue(eq(questionId), any(Pageable.class)))
                .thenReturn(answerPage);

        // Act
        MsgEntity<Page<AnswerEntity>> result = answerService.getAnswersByQuestionId(questionId, page, pageSize, null);

        // Assert
        assertNotNull(result);
        assertEquals("Answers retrieved successfully", result.getCode());
        assertEquals(2, result.getMsg().getTotalElements());
        verify(answerRepository, times(1)).findByQuestionIdAndIsDeletedFalseAndQuestionVisibleTrue(eq(questionId), any(Pageable.class));
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
        when(answerRepository.findByIdAndIsDeletedFalseAndQuestionVisibleTrue(answerId)).thenReturn(existingAnswer);
        when(answerRepository.save(any(AnswerEntity.class))).thenReturn(existingAnswer);

        // Act
        MsgEntity<AnswerEntity> result = answerService.updateAnswer(answerId, content, userId);

        // Assert
        assertNotNull(result);
        assertEquals("Answer updated successfully", result.getCode());
        verify(answerRepository, times(1)).save(any(AnswerEntity.class));
    }

    @Test
    void updateAnswer_NotFound() {
        // Arrange
        Integer answerId = 1;
        String content = "Updated answer content";
        String userId = "user123";
        when(answerRepository.findByIdAndIsDeletedFalseAndQuestionVisibleTrue(answerId)).thenReturn(null);

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
        when(answerRepository.findByIdAndIsDeletedFalseAndQuestionVisibleTrue(answerId)).thenReturn(existingAnswer);

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
        when(answerRepository.findByIdAndIsDeletedFalseAndQuestionVisibleTrue(answerId)).thenReturn(existingAnswer);

        // Act
        MsgEntity<String> result = answerService.deleteAnswer(answerId, userId);

        // Assert
        assertNotNull(result);
        assertEquals("Answer deleted successfully", result.getCode());
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
        when(answerRepository.findByUserIdAndIsDeletedFalseAndQuestionVisibleTrue(eq(username), any(Pageable.class)))
                .thenReturn(answerPage);
        when(questionService.getQuestionById(any())).thenReturn(new QuestionEntity());

        // Act
        MsgEntity<Page<AnswerEntity>> result = answerService.getAnswersByUserId(username, page, pageSize);

        // Assert
        assertNotNull(result);
        assertEquals("Answers retrieved successfully", result.getCode());
        assertEquals(2, result.getMsg().getTotalElements());
        verify(answerRepository, times(1)).findByUserIdAndIsDeletedFalseAndQuestionVisibleTrue(eq(username), any(Pageable.class));
    }

    @Test
    void getAllAnswers_Success() {
        // Arrange
        Integer page = 1;
        Integer pageSize = 10;
        List<AnswerEntity> answers = Arrays.asList(new AnswerEntity(), new AnswerEntity());
        Page<AnswerEntity> answerPage = new PageImpl<>(answers);
        when(answerRepository.findByIsDeletedFalseAndQuestionVisibleTrue(any(Pageable.class))).thenReturn(answerPage);
        when(questionService.getQuestionById(any())).thenReturn(new QuestionEntity());

        // Act
        MsgEntity<Page<AnswerEntity>> result = answerService.getAllAnswers(page, pageSize, null);

        // Assert
        assertNotNull(result);
        assertEquals("All answers retrieved successfully", result.getCode());
        assertEquals(2, result.getMsg().getTotalElements());
        verify(answerRepository, times(1)).findByIsDeletedFalseAndQuestionVisibleTrue(any(Pageable.class));
    }
} 