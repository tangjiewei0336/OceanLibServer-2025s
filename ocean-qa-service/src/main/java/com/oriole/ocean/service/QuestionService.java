package com.oriole.ocean.service;

import com.oriole.ocean.common.po.mongo.QuestionEntity;
import com.oriole.ocean.common.vo.MsgEntity;
import org.springframework.data.domain.Page;

import java.util.List;

public interface QuestionService {
    MsgEntity<Integer> createQuestion(String title, String content, String userId);

    MsgEntity<Page<QuestionEntity>> getQuestions(int page, int pageSize, String username, Integer sortMethod, Boolean includeDeleted);

    QuestionEntity getQuestionById(Integer questionId);

    List<QuestionEntity> getQuestionByIds(List<Integer> questionIds);

    MsgEntity<QuestionEntity> updateQuestion(Integer questionId, String title, String content,
                                             Boolean isPost, Boolean isHide, Integer setReward);

    MsgEntity<String> deleteQuestion(Integer questionId);

    MsgEntity<Page<QuestionEntity>> getMyDrafts(String userId, int page, int pageSize);

    /**
     * 更新问题的回答数
     *
     * @param questionId 问题ID
     * @param delta      回答数的变化量（正数增加，负数减少）
     */
    void updateAnswerCount(Integer questionId, int delta);

    /**
     * 增加问题的浏览次数
     *
     * @param questionId 问题ID
     */
    void incrementViewCount(Integer questionId);

    /**
     * 管理员获取所有问题，无视isDeleted、isHidden、isPosted的限制
     *
     * @param page 页码
     * @param pageSize 每页大小
     * @param sortMethod 排序方式：0-按更新时间，1-按热度
     * @return 问题列表
     */
    MsgEntity<Page<QuestionEntity>> getAllQuestionsForAdmin(int page, int pageSize, Integer sortMethod);
}