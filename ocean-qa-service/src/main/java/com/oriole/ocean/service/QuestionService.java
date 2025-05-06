package com.oriole.ocean.service;

import com.oriole.ocean.common.po.mongo.QuestionEntity;
import com.oriole.ocean.common.vo.MsgEntity;
import org.springframework.data.domain.Page;

public interface QuestionService {
    MsgEntity<String> createQuestion(String title, String content, String userId);

    MsgEntity<Page<QuestionEntity>> getQuestions(int page, int pageSize);

    MsgEntity<QuestionEntity> updateQuestion(String questionId, String title, String content,
                                             Boolean isPost, Boolean isHide, Integer setReward, String userId);

    MsgEntity<String> deleteQuestion(String questionId, String userId);
}