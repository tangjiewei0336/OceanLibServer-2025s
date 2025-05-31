package com.oriole.ocean.integration;

import com.oriole.ocean.common.enumerate.BehaviorType;
import com.oriole.ocean.common.enumerate.MainType;
import com.oriole.ocean.common.po.mongo.QuestionEntity;
import com.oriole.ocean.common.po.mongo.UserBehaviorEntity;
import com.oriole.ocean.common.service.UserBehaviorService;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.repository.MongoQuestionRepository;
import com.oriole.ocean.service.QuestionService;
import com.oriole.ocean.service.SequenceGeneratorService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class QuestionServiceIntegrationTest {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private MongoQuestionRepository mongoQuestionRepository;

    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

    @DubboReference(version = "1.0.0", group = "test")
    private UserBehaviorService userBehaviorService;

    private String testUserId;
    private Integer testQuestionId;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        mongoQuestionRepository.deleteAll();
        
        // 创建测试用户ID
        testUserId = "test_user_" + System.currentTimeMillis();
        
        // 创建测试问题
        MsgEntity<Integer> createResult = questionService.createQuestion(
            "Integration Test Question",
            "This is a test question for integration testing",
            testUserId
        );
        questionService.updateQuestion(
            createResult.getMsg(),
            null,
            null,
            true,  // isPost
            false, // isHide
            null,  // setReward
            testUserId
        );
        testQuestionId = createResult.getMsg();
    }

    @Test
    void testCreateAndRetrieveQuestion() {
        // 创建新问题
        String title = "New Integration Test Question";
        String content = "This is another test question";
        MsgEntity<Integer> createResult = questionService.createQuestion(title, content, testUserId);

        assertNotNull(createResult);
        assertEquals("SUCCESS", createResult.getState());
        assertNotNull(createResult.getMsg());

        // 获取创建的问题
        QuestionEntity retrievedQuestion = questionService.getQuestionById(createResult.getMsg());
        assertNotNull(retrievedQuestion);
        assertEquals(title, retrievedQuestion.getTitle());
        assertEquals(content, retrievedQuestion.getContent());
        assertEquals(testUserId, retrievedQuestion.getUserId());
    }

    @Test
    void testGetQuestionsWithPagination() {
        // 创建多个测试问题
        for (int i = 0; i < 5; i++) {
            Integer questionId = questionService.createQuestion(
                    "Test Question " + i,
                    "Content " + i,
                    testUserId
            ).getMsg();
            questionService.updateQuestion(
                questionId,
                null,
                null,
                true,  // isPost
                false, // isHide
                null,  // setReward
                testUserId
            );
        }

        // 测试分页获取问题
        MsgEntity<Page<QuestionEntity>> result = questionService.getQuestions(1, 3, testUserId, 0, false);
        
        assertNotNull(result);
        assertEquals("SUCCESS", result.getState());
        assertNotNull(result.getMsg());
        assertEquals(3, result.getMsg().getContent().size());
    }

    @Test
    void testUpdateQuestion() {
        // 更新问题
        String newTitle = "Updated Title";
        String newContent = "Updated Content";
        MsgEntity<QuestionEntity> updateResult = questionService.updateQuestion(
            testQuestionId,
            newTitle,
            newContent,
            true,  // isPost
            false, // isHide
            null,  // setReward
            testUserId
        );

        assertNotNull(updateResult);
        assertEquals("SUCCESS", updateResult.getState());
        assertEquals(newTitle, updateResult.getMsg().getTitle());
        assertEquals(newContent, updateResult.getMsg().getContent());
        assertTrue(updateResult.getMsg().getIsPosted());
    }

    @Test
    void testDeleteQuestion() {
        // 删除问题
        MsgEntity<String> deleteResult = questionService.deleteQuestion(testQuestionId, testUserId);

        assertNotNull(deleteResult);
        assertEquals("SUCCESS", deleteResult.getState());

        // 验证问题已被删除
        QuestionEntity deletedQuestion = questionService.getQuestionById(testQuestionId);
        assertNull(deletedQuestion);
    }

    @Test
    void testGetQuestionByIds() {

        Integer msg = questionService.createQuestion("Question 2", "Content 2", testUserId).getMsg();
        assertNotNull(msg);
        Integer msg1 = questionService.createQuestion("Question 3", "Content 3", testUserId).getMsg();


        // 创建多个测试问题
        List<Integer> questionIds = Arrays.asList(
            testQuestionId, msg, msg1
        );

        questionService.updateQuestion(
                msg,
                null,
                null,
                true,  // isPost
                false, // isHide
                null,  // setReward
                testUserId
        );

        questionService.updateQuestion(
                msg1,
                null,
                null,
                true,  // isPost
                false, // isHide
                null,  // setReward
                testUserId
        );

        // 获取多个问题
        List<QuestionEntity> questions = questionService.getQuestionByIds(questionIds);

        assertNotNull(questions);
        assertEquals(3, questions.size());
        questions.forEach(question -> {
            assertTrue(questionIds.contains(question.getBindId()));
            assertEquals(testUserId, question.getUserId());
        });
    }

    @Test
    void testUserBehaviorIntegration() {
        // 获取问题并验证用户行为记录
        QuestionEntity question = questionService.getQuestionById(testQuestionId);
        assertNotNull(question);

        // 验证用户行为记录
        UserBehaviorEntity behavior = new UserBehaviorEntity(
            testQuestionId,
            MainType.QUESTION,
            testUserId,
            BehaviorType.DO_READ
        );
        
        // 这里可以添加对 userBehaviorService 的验证
        // 由于 Dubbo 服务的限制，我们可能需要通过其他方式来验证行为记录
        // 例如，通过查询数据库或调用其他接口
    }
} 