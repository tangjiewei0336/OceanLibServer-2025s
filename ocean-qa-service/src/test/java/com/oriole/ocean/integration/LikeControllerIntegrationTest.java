package com.oriole.ocean.integration;

import com.oriole.ocean.common.auth.AuthUser;
import com.oriole.ocean.common.enumerate.EvaluateType;
import com.oriole.ocean.common.enumerate.MainType;
import com.oriole.ocean.common.po.mongo.AnswerEntity;
import com.oriole.ocean.common.po.mongo.QuestionEntity;
import com.oriole.ocean.common.po.mongo.UserBehaviorEntity;
import com.oriole.ocean.common.service.NotifyService;
import com.oriole.ocean.common.service.UserBehaviorService;
import com.oriole.ocean.common.vo.AuthUserEntity;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.controller.LikeController;
import com.oriole.ocean.service.AnswerService;
import com.oriole.ocean.service.QuestionService;
import com.oriole.ocean.service.SequenceGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class LikeControllerIntegrationTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerService answerService;

    @Autowired
    private LikeController likeController;

    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

    @MockBean
    private UserBehaviorService userBehaviorService;

    @MockBean
    private NotifyService notifyService;

    private Integer testQuestionId;
    private Integer testAnswerId;
    private AuthUserEntity testUser;
    private AuthUserEntity otherUser;

    @BeforeEach
    void setUp() {
        System.out.println("警告：本测试会写入生产环境中的UserBehavior。");
        // 清理测试数据
        mongoTemplate.remove(new Query(), QuestionEntity.class);
        mongoTemplate.remove(new Query(), AnswerEntity.class);
        mongoTemplate.remove(new Query(), UserBehaviorEntity.class);

        // 创建测试用户
        String testUserId = "testUser";
        testUser = new AuthUserEntity(testUserId, "USER");
        String otherUserId = "otherUser";
        otherUser = new AuthUserEntity(otherUserId, "USER");

        // 创建测试问题
        QuestionEntity question = new QuestionEntity();
        question.setBindId(sequenceGeneratorService.getNextSequence("question"));
        question.setUserId(testUserId);
        question.setTitle("Test Question");
        question.setContent("Test Content");
        question.setIsPosted(true);
        question.setIsHidden(false);
        question.setLikeCount(0);
        question.setDislikeCount(0);
        mongoTemplate.save(question);
        testQuestionId = question.getBindId();

        // 创建测试回答
        AnswerEntity answer = new AnswerEntity();
        answer.setId(sequenceGeneratorService.getNextSequence("answer"));
        answer.setQuestionId(testQuestionId);
        answer.setUserId(otherUserId);
        answer.setContent("Test Answer");
        answer.setLikeCount(0);
        answer.setDislikeCount(0);
        mongoTemplate.save(answer);
        testAnswerId = answer.getId();
    }

    @Test
    void testEvaluateQuestion_Like() {
        // 准备测试数据
        when(userBehaviorService.checkAndGetUserEvaluateBehavior(any(), eq(false), eq(true)))
                .thenReturn(Arrays.asList(EvaluateType.LIKE));

        // 执行点赞操作
        MsgEntity<Map<String, Integer>> result = likeController.evaluateQuestion(
                otherUser, testQuestionId, false, true);

        // 验证结果
        assertEquals("SUCCESS", result.getState());
        assertEquals("1", result.getCode());
        Map<String, Integer> data = result.getMsg();
        assertEquals(1, data.get("like_count"));
        assertEquals(0, data.get("dislike_count"));
        assertEquals(1, data.get("net_count"));

        // 验证问题数据更新
        QuestionEntity updatedQuestion = questionService.getQuestionById(testQuestionId);
        assertEquals(1, updatedQuestion.getLikeCount());
        assertEquals(0, updatedQuestion.getDislikeCount());

        // 验证通知发送
        verify(notifyService, times(1)).addNotify(any());
    }

    @Test
    void testEvaluateQuestion_Dislike() {
        // 准备测试数据
        when(userBehaviorService.checkAndGetUserEvaluateBehavior(any(), eq(false), eq(false)))
                .thenReturn(Arrays.asList(EvaluateType.DISLIKE));

        // 执行点踩操作
        MsgEntity<Map<String, Integer>> result = likeController.evaluateQuestion(
                otherUser, testQuestionId, false, false);

        // 验证结果
        assertEquals("SUCCESS", result.getState());
        assertEquals("1", result.getCode());
        Map<String, Integer> data = result.getMsg();
        assertEquals(0, data.get("like_count"));
        assertEquals(1, data.get("dislike_count"));
        assertEquals(-1, data.get("net_count"));

        // 验证问题数据更新
        QuestionEntity updatedQuestion = questionService.getQuestionById(testQuestionId);
        assertEquals(0, updatedQuestion.getLikeCount());
        assertEquals(1, updatedQuestion.getDislikeCount());

        // 验证没有发送通知
        verify(notifyService, never()).addNotify(any());
    }

    @Test
    void testEvaluateQuestion_CancelLike() {
        // 准备测试数据
        when(userBehaviorService.checkAndGetUserEvaluateBehavior(any(), eq(true), eq(true)))
                .thenReturn(Arrays.asList(EvaluateType.CANCEL_LIKE));

        // 先点赞
        likeController.evaluateQuestion(otherUser, testQuestionId, false, true);

        // 取消点赞
        MsgEntity<Map<String, Integer>> result = likeController.evaluateQuestion(
                otherUser, testQuestionId, true, true);

        // 验证结果
        assertEquals("SUCCESS", result.getState());
        assertEquals("1", result.getCode());
        Map<String, Integer> data = result.getMsg();
        assertEquals(0, data.get("like_count"));
        assertEquals(0, data.get("dislike_count"));
        assertEquals(0, data.get("net_count"));

        // 验证问题数据更新
        QuestionEntity updatedQuestion = questionService.getQuestionById(testQuestionId);
        assertEquals(0, updatedQuestion.getLikeCount());
        assertEquals(0, updatedQuestion.getDislikeCount());
    }

    @Test
    void testEvaluateQuestion_SelfEvaluation() {
        // 尝试对自己的问题进行评价
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                likeController.evaluateQuestion(testUser, testQuestionId, false, true));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Cannot evaluate your own question", exception.getReason());
    }

    @Test
    void testEvaluateAnswer_Like() {
        // 准备测试数据
        when(userBehaviorService.checkAndGetUserEvaluateBehavior(any(), eq(false), eq(true)))
                .thenReturn(Arrays.asList(EvaluateType.LIKE));

        // 执行点赞操作
        MsgEntity<Object> result = likeController.evaluateAnswer(
                testUser, testAnswerId, false, true);

        // 验证结果
        assertEquals("SUCCESS", result.getState());
        assertEquals("1", result.getCode());
        Map<String, Integer> data = (Map<String, Integer>) result.getMsg();
        assertEquals(1, data.get("like_count"));
        assertEquals(0, data.get("dislike_count"));
        assertEquals(1, data.get("net_count"));

        // 验证回答数据更新
        AnswerEntity updatedAnswer = answerService.getAnswerById(testAnswerId);
        assertEquals(1, updatedAnswer.getLikeCount());
        assertEquals(0, updatedAnswer.getDislikeCount());

        // 验证通知发送
        verify(notifyService, times(1)).addNotify(any());
    }

    @Test
    void testEvaluateAnswer_Dislike() {
        // 准备测试数据
        when(userBehaviorService.checkAndGetUserEvaluateBehavior(any(), eq(false), eq(false)))
                .thenReturn(Arrays.asList(EvaluateType.DISLIKE));

        // 执行点踩操作
        MsgEntity<Object> result = likeController.evaluateAnswer(
                testUser, testAnswerId, false, false);

        // 验证结果
        assertEquals("SUCCESS", result.getState());
        assertEquals("1", result.getCode());
        Map<String, Integer> data = (Map<String, Integer>) result.getMsg();
        assertEquals(0, data.get("like_count"));
        assertEquals(1, data.get("dislike_count"));
        assertEquals(-1, data.get("net_count"));

        // 验证回答数据更新
        AnswerEntity updatedAnswer = answerService.getAnswerById(testAnswerId);
        assertEquals(0, updatedAnswer.getLikeCount());
        assertEquals(1, updatedAnswer.getDislikeCount());

        // 验证没有发送通知
        verify(notifyService, never()).addNotify(any());
    }

    @Test
    void testEvaluateAnswer_SelfEvaluation() {
        // 尝试对自己的回答进行评价
        MsgEntity<Object> result = likeController.evaluateAnswer(
                otherUser, testAnswerId, false, true);

        // 验证结果
        assertEquals("-1", result.getState());
        assertEquals("ERROR", result.getCode());
        assertEquals("不能给自己的回答点赞或者点踩。", result.getMsg());
    }
} 