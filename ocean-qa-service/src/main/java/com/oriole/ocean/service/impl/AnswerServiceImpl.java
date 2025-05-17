package com.oriole.ocean.service.impl;

import com.oriole.ocean.common.po.mongo.AnswerEntity;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.repository.AnswerRepository;
import com.oriole.ocean.service.AnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

@Service
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepository;

    @Autowired
    public AnswerServiceImpl(AnswerRepository answerRepository) {
        this.answerRepository = answerRepository;
    }

    @Override
    public MsgEntity<Integer> submitAnswer(Integer questionId, String content, String userId) {
        AnswerEntity answer = new AnswerEntity();
        answer.setQuestionId(questionId);
        answer.setUserId(userId);
        answer.setContent(content);
        answer.setCreateTime(new Date());
        answer.setUpdateTime(new Date());

        AnswerEntity savedAnswer = answerRepository.save(answer);

        return new MsgEntity<>("SUCCESS", "Answer submitted successfully", savedAnswer.getId());
    }

    @Override
    public MsgEntity<Page<AnswerEntity>> getAnswersByQuestionId(Integer questionId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<AnswerEntity> answers = answerRepository.findByQuestionIdAndIsDeletedFalse(questionId, pageable);
        return new MsgEntity<>("SUCCESS", "Answers retrieved successfully", answers);
    }

    @Override
    public MsgEntity<AnswerEntity> updateAnswer(Integer answerId, String content, String userId) {
        AnswerEntity answer = answerRepository.findByIdAndIsDeletedFalse(answerId);
        if (answer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Answer not found");
        }


        if (!answer.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to update this answer");
        }

        answer.setContent(content);
        answer.setUpdateTime(new Date());
        AnswerEntity updatedAnswer = answerRepository.save(answer);

        return new MsgEntity<>("SUCCESS", "Answer updated successfully", updatedAnswer);
    }

    @Override
    public MsgEntity<String> deleteAnswer(Integer answerId, String userId) {
        AnswerEntity answer = answerRepository.findByIdAndIsDeletedFalse(answerId);
        if (answer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Answer not found");
        }

        if (!answer.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to delete this answer");
        }

        answer.setIsDeleted(true);
        answer.setUpdateTime(new Date());
        answerRepository.save(answer);

        return new MsgEntity<>("SUCCESS", "Answer deleted successfully", null);
    }
}