package com.oriole.ocean.service;

import com.oriole.ocean.common.po.mongo.AnswerEntity;
import com.oriole.ocean.common.vo.MsgEntity;
import org.springframework.data.domain.Page;

public interface AnswerService {
    MsgEntity<Integer> submitAnswer(Integer questionId, String content, String userId);

    MsgEntity<Page<AnswerEntity>> getAnswersByQuestionId(Integer questionId, int page, int pageSize);

    MsgEntity<AnswerEntity> updateAnswer(Integer answerId, String content, String userId);

    MsgEntity<String> deleteAnswer(Integer answerId, String userId);
}