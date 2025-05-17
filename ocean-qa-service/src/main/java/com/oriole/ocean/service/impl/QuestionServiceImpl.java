package com.oriole.ocean.service.impl;

import com.oriole.ocean.common.po.mongo.QuestionEntity;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.repository.QuestionRepository;
import com.oriole.ocean.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;

    @Autowired
    public QuestionServiceImpl(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    public MsgEntity<Integer> createQuestion(String title, String content, String userId) {
        QuestionEntity question = new QuestionEntity();
        question.setUserId(userId);
        question.setTitle(title != null ? title : "");
        question.setContent(content != null ? content : "");
        question.setCreateTime(new Date());
        question.setUpdateTime(new Date());

        QuestionEntity savedQuestion = questionRepository.save(question);
        return new MsgEntity<>("SUCCESS", "Question created successfully", savedQuestion.getId());
    }

    @Override
    public MsgEntity<Page<QuestionEntity>> getQuestions(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<QuestionEntity> questions = questionRepository.findByIsDeletedFalse(pageable);
        return new MsgEntity<>("SUCCESS", "Questions retrieved successfully", questions);
    }

    @Override
    public QuestionEntity getQuestionById(Integer questionId) {
        QuestionEntity question = questionRepository.findByIdAndIsDeletedFalse(questionId);
        if (question == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found");
        }
        return question;
    }

    @Override
    public List<QuestionEntity> getQuestionByIds(List<Integer> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<QuestionEntity> questions = questionRepository.findByIdInAndIsDeletedFalse(questionIds);
        if (questions == null || questions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No questions found for the provided IDs");
        }
        return questions;
    }

    @Override
    public MsgEntity<QuestionEntity> updateQuestion(Integer questionId, String title, String content,
                                                    Boolean isPost, Boolean isHide, Integer setReward, String userId) {
        QuestionEntity question = questionRepository.findByIdAndIsDeletedFalse(questionId);
        if (question == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found");
        }

        if (!question.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to update this question");
        }

        // Validate post/hide states
        if (isPost != null && isHide != null && isPost && isHide) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question cannot be both posted and hidden");
        }

        if (title != null) {
            question.setTitle(title);
        }

        if (content != null) {
            question.setContent(content);
        }

        if (isPost != null) {
            question.setIsPosted(isPost);
            question.setIsHidden(false);
        }

        if (isHide != null) {
            question.setIsHidden(isHide);
            question.setIsPosted(false);
        }

        if (setReward != null && setReward > 0) {
            question.setRewardPoints(setReward);
        }

        question.setUpdateTime(new Date());
        QuestionEntity updatedQuestion = questionRepository.save(question);

        return new MsgEntity<>("SUCCESS", "Question updated successfully", updatedQuestion);
    }

    @Override
    public MsgEntity<String> deleteQuestion(Integer questionId, String userId) {
        QuestionEntity question = questionRepository.findByIdAndIsDeletedFalse(questionId);
        if (question == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found");
        }

        if (!question.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to delete this question");
        }

        question.setIsDeleted(true);
        question.setUpdateTime(new Date());
        questionRepository.save(question);

        return new MsgEntity<>("SUCCESS", "Question deleted successfully", null);
    }
}