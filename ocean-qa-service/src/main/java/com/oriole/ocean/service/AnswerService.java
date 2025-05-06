package com.oriole.ocean.service;

import com.oriole.ocean.common.po.mongo.AnswerEntity;
import com.oriole.ocean.common.vo.MsgEntity;
import org.springframework.data.domain.Page;

public interface AnswerService {
    MsgEntity<String> submitAnswer(String questionId, String content, String userId);

    MsgEntity<Page<AnswerEntity>> getAnswersByQuestionId(String questionId, int page, int pageSize);

    MsgEntity<AnswerEntity> updateAnswer(String answerId, String content, String userId);

    MsgEntity<String> deleteAnswer(String answerId, String userId);
}