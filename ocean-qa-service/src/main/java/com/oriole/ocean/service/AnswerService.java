package com.oriole.ocean.service;

import com.oriole.ocean.common.po.mongo.AnswerEntity;
import com.oriole.ocean.common.vo.MsgEntity;
import org.springframework.data.domain.Page;

import javax.validation.Valid;

public interface AnswerService {
    MsgEntity<Integer> submitAnswer(Integer questionId, String content, String userId);

    MsgEntity<Page<AnswerEntity>> getAnswersByQuestionId(Integer questionId, int page, int pageSize);

    MsgEntity<AnswerEntity> updateAnswer(Integer answerId, String content, String userId);

    MsgEntity<String> deleteAnswer(Integer answerId, String userId);

    MsgEntity<Page<AnswerEntity>> getAnswersByUserId(String username, @Valid Integer page, @Valid Integer pageSize);

    MsgEntity<Page<AnswerEntity>> getAllAnswers(@Valid Integer page, @Valid Integer pageSize);

    /**
     * 根据回答ID获取回答信息
     * @param answerId 回答ID
     * @return 回答实体
     */
    AnswerEntity getAnswerById(Integer answerId);
}