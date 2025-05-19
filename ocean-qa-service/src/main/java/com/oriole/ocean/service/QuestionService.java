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
                                             Boolean isPost, Boolean isHide, Integer setReward, String userId);

    MsgEntity<String> deleteQuestion(Integer questionId, String userId);
}