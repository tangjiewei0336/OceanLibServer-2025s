package com.oriole.ocean.service.impl;

import com.oriole.ocean.common.po.mongo.QuestionEntity;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.repository.MongoQuestionRepository;
import com.oriole.ocean.service.QuestionService;
import com.oriole.ocean.service.SequenceGeneratorService;
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

    private final MongoQuestionRepository mongoQuestionRepository;

    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;


    @Autowired
    public QuestionServiceImpl(MongoQuestionRepository mongoQuestionRepository) {
        this.mongoQuestionRepository = mongoQuestionRepository;
    }

    @Override
    public MsgEntity<Integer> createQuestion(String title, String content, String userId) {
        QuestionEntity question = new QuestionEntity();
        question.setUserId(userId);
        question.setTitle(title != null ? title : "");
        question.setContent(content != null ? content : "");
        question.setCreateTime(new Date());
        question.setUpdateTime(new Date());
        question.setBindId(sequenceGeneratorService.getNextSequence("questions"));

        QuestionEntity savedQuestion = mongoQuestionRepository.save(question);
        return new MsgEntity<>("SUCCESS", "Question created successfully", savedQuestion.getBindId());
    }

    @Override
    public MsgEntity<Page<QuestionEntity>> getQuestions(int page, int pageSize, String username, Integer sortMethod, Boolean includeDeleted) {
        // 当username为空时，展示所有的问题；不为空时则展示这个人提出的问题。 默认按照时间更新顺序。
        // sortMethod: 0:时间更新 1:热度

        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<QuestionEntity> questions;
        if (username != null && !username.isEmpty()) {
            questions = mongoQuestionRepository.findByUserIdAndIsDeletedFalse(username, pageable);
        } else {
            questions = mongoQuestionRepository.findByUserId(username, pageable);
        }

        if (questions == null || questions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No questions found");
        }
        if (includeDeleted != null && includeDeleted) {
            questions = mongoQuestionRepository.findAll(pageable);
        } else {
            questions = mongoQuestionRepository.findByIsDeletedFalse(pageable);
        }
        if (sortMethod != null) {
            switch (sortMethod) {
                case 0: // 按更新时间排序
                    pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "updateTime"));
                    break;
                case 1: // 按热度排序（假设热度是通过回答数和浏览数计算的）
                    pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "viewCount", "answerCount"));
                    break;
                default:
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sort method");
            }
            questions = mongoQuestionRepository.findByIsDeletedFalse(pageable);
        }
        if (questions == null || questions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No questions found");
        }
        return new MsgEntity<>("SUCCESS", "Questions retrieved successfully", questions);

    }

//    @Override
//    public MsgEntity<Page<QuestionEntity>> getQuestions(int page, int pageSize) {
//        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
//        Page<QuestionEntity> questions = questionRepository.findByIsDeletedFalse(pageable);
//        return new MsgEntity<>("SUCCESS", "Questions retrieved successfully", questions);
//    }

    @Override
    public QuestionEntity getQuestionById(Integer questionId) {
        QuestionEntity question = mongoQuestionRepository.findByBindIdAndIsDeletedFalse(questionId);
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
        List<QuestionEntity> questions = mongoQuestionRepository.findByBindIdInAndIsDeletedFalse(questionIds);
        if (questions == null || questions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No questions found for the provided IDs");
        }
        return questions;
    }

    @Override
    public MsgEntity<QuestionEntity> updateQuestion(Integer questionId, String title, String content,
                                                    Boolean isPost, Boolean isHide, Integer setReward, String userId) {
        QuestionEntity question = mongoQuestionRepository.findByBindIdAndIsDeletedFalse(questionId);
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
        QuestionEntity updatedQuestion = mongoQuestionRepository.save(question);

        return new MsgEntity<>("SUCCESS", "Question updated successfully", updatedQuestion);
    }

    @Override
    public MsgEntity<String> deleteQuestion(Integer questionId, String userId) {
        QuestionEntity question = mongoQuestionRepository.findByBindIdAndIsDeletedFalse(questionId);
        if (question == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found");
        }

        if (!question.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to delete this question");
        }

        question.setIsDeleted(true);
        question.setUpdateTime(new Date());
        mongoQuestionRepository.save(question);

        return new MsgEntity<>("SUCCESS", "Question deleted successfully", null);
    }
}