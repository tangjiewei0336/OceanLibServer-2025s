package com.oriole.ocean.service.impl;

import com.oriole.ocean.common.po.mongo.AnswerEntity;
import com.oriole.ocean.common.po.mongo.QuestionEntity;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.repository.AnswerRepository;
import com.oriole.ocean.service.AnswerService;
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

import java.util.Date;
import java.util.List;

@Service
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepository;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

    @Autowired
    public AnswerServiceImpl(AnswerRepository answerRepository) {
        this.answerRepository = answerRepository;
    }

    @Override
    public MsgEntity<Integer> submitAnswer(Integer questionId, String content, String userId) {
        AnswerEntity answer = new AnswerEntity();
        QuestionEntity questionById = questionService.getQuestionById(questionId);
        if(!(questionById.getIsPosted().equals(Boolean.TRUE) && questionById.getIsDeleted().equals(Boolean.FALSE)
                                && questionById.getIsHidden().equals(Boolean.FALSE))){
            return new MsgEntity<>("FAIL", "Question not visible", -1);
        }
        answer.setId(sequenceGeneratorService.getNextSequence("answer"));
        answer.setQuestionId(questionId);
        answer.setUserId(userId);
        answer.setContent(content);
        answer.setQuestionVisible(true);
        answer.setCreateTime(new Date());
        answer.setUpdateTime(new Date());

        AnswerEntity savedAnswer = answerRepository.save(answer);

        return new MsgEntity<>("SUCCESS", "Answer submitted successfully", savedAnswer.getId());
    }

    @Override
    public MsgEntity<Page<AnswerEntity>> getAnswersByQuestionId(Integer questionId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<AnswerEntity> answers = answerRepository.findByQuestionIdAndIsDeletedFalseAndQuestionVisibleTrue(questionId, pageable);
        return new MsgEntity<>("SUCCESS", "Answers retrieved successfully", answers);
    }

    @Override
    public MsgEntity<AnswerEntity> updateAnswer(Integer answerId, String content, String userId) {
        AnswerEntity answer = answerRepository.findByIdAndIsDeletedFalseAndQuestionVisibleTrue(answerId);
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
        AnswerEntity answer = answerRepository.findByIdAndIsDeletedFalseAndQuestionVisibleTrue(answerId);
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

    @Override
    public MsgEntity<Page<AnswerEntity>> getAnswersByUserId(String username, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<AnswerEntity> answers = answerRepository.findByUserIdAndIsDeletedFalseAndQuestionVisibleTrue(username, pageable);
        if (answers == null || answers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No answers found for the provided user ID");
        }
        for (AnswerEntity answer : answers) {
            // Assuming you have a method to get the question by ID
            answer.setQuestion(questionService.getQuestionById(answer.getQuestionId()));
        }

        return new MsgEntity<>("SUCCESS", "Answers retrieved successfully", answers);
    }

    @Override
    public MsgEntity<Page<AnswerEntity>> getAllAnswers(Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<AnswerEntity> answers = answerRepository.findByIsDeletedFalseAndQuestionVisibleTrue(pageable);
        if (answers == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No answers found");
        }
        for (AnswerEntity answer : answers) {
            answer.setQuestion(questionService.getQuestionById(answer.getQuestionId()));
        }

        return new MsgEntity<>("SUCCESS", "All answers retrieved successfully", answers);
    }

    @Override
    public AnswerEntity getAnswerById(Integer answerId) {
        AnswerEntity answer = answerRepository.findByIdAndIsDeletedFalseAndQuestionVisibleTrue(answerId);
        if (answer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Answer not found");
        }
        return answer;
    }

    @Override
    public int makeAnswerVisible(Integer questionId, boolean visibility) {
        List<AnswerEntity> answers = answerRepository.findByQuestionIdAndQuestionVisibleTrue(questionId);
        for (AnswerEntity answer : answers) {
            answer.setQuestionVisible(visibility);
        }
        answerRepository.saveAll(answers);
        return answers.size();
    }
}