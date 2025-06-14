package com.oriole.ocean.service.impl;

import com.alibaba.nacos.shaded.javax.annotation.Nullable;
import com.oriole.ocean.common.enumerate.BehaviorType;
import com.oriole.ocean.common.enumerate.MainType;
import com.oriole.ocean.common.enumerate.UserInfoLevel;
import com.oriole.ocean.common.po.mongo.AnswerEntity;
import com.oriole.ocean.common.po.mongo.QuestionEntity;
import com.oriole.ocean.common.po.mongo.UserBehaviorEntity;
import com.oriole.ocean.common.po.mysql.UserEntity;
import com.oriole.ocean.common.service.UserBehaviorService;
import com.oriole.ocean.common.service.UserInfoService;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.repository.AnswerRepository;
import com.oriole.ocean.service.AnswerService;
import com.oriole.ocean.service.QuestionService;
import com.oriole.ocean.service.SequenceGeneratorService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

@Service
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionService questionService;
    private final SequenceGeneratorService sequenceGeneratorService;

    @DubboReference
    private UserBehaviorService userBehaviorService;

    @DubboReference
    private UserInfoService userInfoService;

    @Autowired
    public AnswerServiceImpl(AnswerRepository answerRepository,
                             QuestionService questionService,
                             SequenceGeneratorService sequenceGeneratorService) {
        this.answerRepository = answerRepository;
        this.questionService = questionService;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    /**
     * 补充回答的详细信息，包括点赞状态和用户头像
     *
     * @param answer      回答实体
     * @param currentUser 当前用户
     */
    private void enrichAnswerDetails(AnswerEntity answer, String currentUser) {
        // 添加点赞状态
        UserBehaviorEntity likeQuery = new UserBehaviorEntity(answer.getId(), MainType.ANSWER, currentUser, BehaviorType.DO_LIKE);
        List<UserBehaviorEntity> likeBehaviors = userBehaviorService.findAllBehaviorRecords(likeQuery);
        answer.setIsLiked(!likeBehaviors.isEmpty());

        UserBehaviorEntity dislikeQuery = new UserBehaviorEntity(answer.getId(), MainType.ANSWER, currentUser, BehaviorType.DO_DISLIKE);
        List<UserBehaviorEntity> dislikeBehaviors = userBehaviorService.findAllBehaviorRecords(dislikeQuery);
        answer.setIsDisliked(!dislikeBehaviors.isEmpty());

        // 添加用户头像
        UserEntity userInfo = userInfoService.getUserInfo(answer.getUserId(), UserInfoLevel.LIMITED);
        if (userInfo != null) {
            answer.setAvatar(userInfo.getAvatar());
        }
    }

    @Override
    public MsgEntity<Integer> submitAnswer(Integer questionId, String content, String userId) {
        AnswerEntity answer = new AnswerEntity();
        QuestionEntity questionById = questionService.getQuestionById(questionId);
        if (!(questionById.getIsPosted().equals(Boolean.TRUE) && questionById.getIsDeleted().equals(Boolean.FALSE)
                && questionById.getIsHidden().equals(Boolean.FALSE))) {
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
        questionService.updateAnswerCount(questionId, 1);

        return new MsgEntity<>("SUCCESS", "Answer submitted successfully", savedAnswer.getId());
    }

    @Override
    public MsgEntity<Page<AnswerEntity>> getAnswersByQuestionId(Integer questionId, int page, int pageSize, String username, @Valid Boolean includeDeleted) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<AnswerEntity> answers;
        if (includeDeleted) {
            answers = answerRepository.findByQuestionIdAndIsDeletedFalseAndQuestionVisibleTrue(questionId, pageable);
        } else {
            answers = answerRepository.findByQuestionIdAndQuestionVisibleTrue(questionId, pageable);
        }

        // 为每个回答添加详细信息
        for (AnswerEntity answer : answers) {
            enrichAnswerDetails(answer, username); // 这里传入null是因为这个方法不需要用户信息
        }

        return new MsgEntity<>("SUCCESS", "Answers retrieved successfully", answers);
    }

    @Override
    public MsgEntity<AnswerEntity> updateAnswer(Integer answerId, String content, String userId, boolean admin) {
        AnswerEntity answer = answerRepository.findByIdAndIsDeletedFalseAndQuestionVisibleTrue(answerId);
        if (answer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Answer not found");
        }

        if (!answer.getUserId().equals(userId)) {
            if (!admin) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to update this answer");
            }
        }

        answer.setContent(content);
        answer.setUpdateTime(new Date());
        AnswerEntity updatedAnswer = answerRepository.save(answer);

        return new MsgEntity<>("SUCCESS", "Answer updated successfully", updatedAnswer);
    }

    @Override
    public MsgEntity<String> deleteAnswer(Integer answerId) {
        AnswerEntity answer = answerRepository.findByIdAndIsDeletedFalseAndQuestionVisibleTrue(answerId);
        if (answer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Answer not found");
        }

        answer.setIsDeleted(true);
        answer.setUpdateTime(new Date());
        answerRepository.save(answer);

        questionService.updateAnswerCount(answer.getQuestionId(), -1);

        return new MsgEntity<>("SUCCESS", "Answer deleted successfully", null);
    }

    @Override
    public MsgEntity<Page<AnswerEntity>> getAnswersByUserId(String username, Integer page, Integer pageSize, boolean includeDeleted) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<AnswerEntity> answers;
        if (!includeDeleted) {
            answers = answerRepository.findByUserIdAndIsDeletedFalseAndQuestionVisibleTrue(username, pageable);
        } else {
            answers = answerRepository.findByUserId(username, pageable);
        }

        if (answers == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve answers");
        }

        for (AnswerEntity answer : answers) {
            answer.setQuestion(questionService.getQuestionById(answer.getQuestionId()));
            enrichAnswerDetails(answer, username);
        }

        return new MsgEntity<>("SUCCESS", "Answers retrieved successfully", answers);
    }

    @Override
    public MsgEntity<Page<AnswerEntity>> getAllAnswers(Integer page, Integer pageSize, String username, @Valid boolean includeDeleted) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<AnswerEntity> answers;

        if (!includeDeleted) {
            answers = answerRepository.findByIsDeletedFalseAndQuestionVisibleTrue(pageable);
        } else {
            answers = answerRepository.findAll(pageable);
        }

        if (answers == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No answers found");
        }

        for (AnswerEntity answer : answers) {
            answer.setQuestion(questionService.getQuestionById(answer.getQuestionId()));
            enrichAnswerDetails(answer, username);
        }

        return new MsgEntity<>("SUCCESS", "All answers retrieved successfully", answers);
    }

    @Override
    public AnswerEntity getAnswerById(Integer answerId, @Nullable String username) {
        AnswerEntity answer = answerRepository.findByIdAndIsDeletedFalseAndQuestionVisibleTrue(answerId);
        if (answer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Answer not found");
        }
        if (username != null)
            enrichAnswerDetails(answer, username);
        return answer;
    }

    @Override
    public int makeAnswerVisible(Integer questionId, boolean visibility) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE); // Fetch all answers
        List<AnswerEntity> answers = answerRepository.findByQuestionIdAndQuestionVisibleTrue(questionId, pageable).getContent();
        for (AnswerEntity answer : answers) {
            answer.setQuestionVisible(visibility);
        }
        answerRepository.saveAll(answers);
        return answers.size();
    }
}