package com.oriole.ocean.integration;

import com.oriole.ocean.common.po.mongo.AnswerEntity;
import com.oriole.ocean.common.po.mongo.QuestionEntity;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.repository.AnswerRepository;
import com.oriole.ocean.service.AnswerService;
import com.oriole.ocean.service.QuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AnswerServiceIntegrationTest {

    @Autowired
    private AnswerService answerService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerRepository answerRepository;

    private String testUserId;
    private Integer testQuestionId;
    private Integer testAnswerId;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        answerRepository.deleteAll();
        
        // 创建测试用户ID
        testUserId = "test_user_" + System.currentTimeMillis();
        
        // 创建测试问题
        MsgEntity<Integer> createQuestionResult = questionService.createQuestion(
            "Integration Test Question",
            "This is a test question for integration testing",
            testUserId
        );
        testQuestionId = createQuestionResult.getMsg();
        
        // 发布问题
        questionService.updateQuestion(
            testQuestionId,
            null,
            null,
            true,  // isPost
            false, // isHide
            null,  // setReward
            testUserId
        );
        
        // 创建测试答案
        MsgEntity<Integer> createAnswerResult = answerService.submitAnswer(
            testQuestionId,
            "This is a test answer",
            testUserId
        );
        testAnswerId = createAnswerResult.getMsg();
    }

    @Test
    void testSubmitAnswer_Success() {
        // 提交新答案
        String content = "New test answer content";
        MsgEntity<Integer> result = answerService.submitAnswer(testQuestionId, content, testUserId);

        assertNotNull(result);
        assertEquals("SUCCESS", result.getState());
        assertNotNull(result.getMsg());
        assertTrue(result.getMsg() > 0);

        // 验证答案是否保存成功
        AnswerEntity savedAnswer = answerService.getAnswerById(result.getMsg());
        assertNotNull(savedAnswer);
        assertEquals(content, savedAnswer.getContent());
        assertEquals(testUserId, savedAnswer.getUserId());
        assertEquals(testQuestionId, savedAnswer.getQuestionId());
    }

    @Test
    void testSubmitAnswer_QuestionNotVisible() {
        // 隐藏问题
        questionService.updateQuestion(
            testQuestionId,
            null,
            null,
            false, // isPost
            true,  // isHide
            null,  // setReward
            testUserId
        );

        // 尝试提交答案
        MsgEntity<Integer> result = answerService.submitAnswer(
            testQuestionId,
            "This answer should not be submitted",
            testUserId
        );

        assertEquals("FAIL", result.getState());
        assertEquals("Question not visible", result.getCode());
        assertEquals(-1, result.getMsg());
    }

    @Test
    void testGetAnswersByQuestionId() {
        // 创建多个答案
        for (int i = 0; i < 5; i++) {
            answerService.submitAnswer(
                testQuestionId,
                "Test answer " + i,
                testUserId
            );
        }

        // 获取问题的答案
        MsgEntity<Page<AnswerEntity>> result = answerService.getAnswersByQuestionId(testQuestionId, 1, 3);

        assertNotNull(result);
        assertEquals("SUCCESS", result.getState());
        assertNotNull(result.getMsg());
        assertEquals(3, result.getMsg().getContent().size());
    }

    @Test
    void testUpdateAnswer_Success() {
        // 更新答案
        String newContent = "Updated answer content";
        MsgEntity<AnswerEntity> result = answerService.updateAnswer(testAnswerId, newContent, testUserId);

        assertNotNull(result);
        assertEquals("SUCCESS", result.getState());
        assertEquals(newContent, result.getMsg().getContent());
    }

    @Test
    void testUpdateAnswer_Unauthorized() {
        // 尝试用其他用户更新答案
        String otherUserId = "other_user_" + System.currentTimeMillis();
        assertThrows(ResponseStatusException.class, () ->
            answerService.updateAnswer(testAnswerId, "Unauthorized update", otherUserId)
        );
    }

    @Test
    void testDeleteAnswer_Success() {
        // 删除答案
        MsgEntity<String> result = answerService.deleteAnswer(testAnswerId, testUserId);

        assertNotNull(result);
        assertEquals("SUCCESS", result.getState());

        // 验证答案已被删除
        assertThrows(ResponseStatusException.class, () ->
            answerService.getAnswerById(testAnswerId)
        );
    }

    @Test
    void testDeleteAnswer_Unauthorized() {
        // 尝试用其他用户删除答案
        String otherUserId = "other_user_" + System.currentTimeMillis();
        assertThrows(ResponseStatusException.class, () ->
            answerService.deleteAnswer(testAnswerId, otherUserId)
        );
    }

    @Test
    void testGetAnswersByUserId() {
        // 创建多个答案
        for (int i = 0; i < 5; i++) {
            answerService.submitAnswer(
                testQuestionId,
                "Test answer " + i,
                testUserId
            );
        }

        // 获取用户的答案
        MsgEntity<Page<AnswerEntity>> result = answerService.getAnswersByUserId(testUserId, 1, 3);

        assertNotNull(result);
        assertEquals("SUCCESS", result.getState());
        assertNotNull(result.getMsg());
        assertEquals(3, result.getMsg().getContent().size());
        
        // 验证答案包含问题信息
        result.getMsg().getContent().forEach(answer -> {
            assertNotNull(answer.getQuestion());
            assertEquals(testQuestionId, answer.getQuestion().getBindId());
        });
    }

    @Test
    void testGetAllAnswers() {
        // 创建多个答案
        for (int i = 0; i < 5; i++) {
            answerService.submitAnswer(
                testQuestionId,
                "Test answer " + i,
                testUserId
            );
        }

        // 获取所有答案
        MsgEntity<Page<AnswerEntity>> result = answerService.getAllAnswers(1, 3);

        assertNotNull(result);
        assertEquals("SUCCESS", result.getState());
        assertNotNull(result.getMsg());
        assertEquals(3, result.getMsg().getContent().size());
        
        // 验证答案包含问题信息
        result.getMsg().getContent().forEach(answer -> {
            assertNotNull(answer.getQuestion());
            assertEquals(testQuestionId, answer.getQuestion().getBindId());
        });
    }

    @Test
    void testMakeAnswerVisible() {
        // 创建多个答案
        for (int i = 0; i < 5; i++) {
            answerService.submitAnswer(
                testQuestionId,
                "Test answer " + i,
                testUserId
            );
        }

        // 更新答案可见性
        int updatedCount = answerService.makeAnswerVisible(testQuestionId, false);
        assertEquals(6, updatedCount); // 5个新答案 + 1个setUp中创建的答案

        // 验证答案已隐藏
        MsgEntity<Page<AnswerEntity>> result = answerService.getAnswersByQuestionId(testQuestionId, 1, 10);
        assertEquals(0, result.getMsg().getContent().size());
    }
} 