package com.oriole.ocean.service.impl;

import com.alibaba.nacos.shaded.javax.annotation.Nullable;
import com.oriole.ocean.common.enumerate.BehaviorType;
import com.oriole.ocean.common.enumerate.MainType;
import com.oriole.ocean.common.po.mongo.QuestionEntity;
import com.oriole.ocean.common.po.mongo.UserBehaviorEntity;
import com.oriole.ocean.common.service.UserBehaviorService;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.repository.MongoQuestionRepository;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class QuestionServiceImpl implements QuestionService {

    private final MongoQuestionRepository mongoQuestionRepository;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final AnswerService answerService;
    
    @DubboReference
    private UserBehaviorService userBehaviorService;

    @Autowired
    public QuestionServiceImpl(MongoQuestionRepository mongoQuestionRepository,
                             SequenceGeneratorService sequenceGeneratorService,
                             @Lazy AnswerService answerService) {
        this.mongoQuestionRepository = mongoQuestionRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.answerService = answerService;
    }

    @Override
    public MsgEntity<Integer> createQuestion(String title, String content, String userId) {
        QuestionEntity question = new QuestionEntity();
        question.setUserId(userId);
        question.setTitle(title != null ? title : "");
        question.setContent(content != null ? content : "");
        question.setCreateTime(new Date());
        question.setUpdateTime(new Date());
        question.setIsPosted(false);
        question.setBindId(sequenceGeneratorService.getNextSequence("questions"));

        QuestionEntity savedQuestion = mongoQuestionRepository.save(question);

        // Record user behavior for question creation
        incrementViewCount(savedQuestion.getBindId());

        return new MsgEntity<>("SUCCESS", "Question created successfully", savedQuestion.getBindId());
    }

    @Override
    public MsgEntity<Page<QuestionEntity>> getQuestions(int page, int pageSize, String username, Integer sortMethod, Boolean includeDeleted) {
        // 当username为空时，展示所有的问题；不为空时则展示这个人提出的问题。 默认按照时间更新顺序。
        // sortMethod: 0:时间更新 1:热度

        Pageable pageable;
        Page<QuestionEntity> questions;

        if (sortMethod != null) {
            switch (sortMethod) {
                case 0: // 按更新时间排序
                    pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "updateTime"));
                    break;
                case 1: // 按热度排序（假设热度是通过回答数和浏览数计算的）
                    pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "viewCount"));
                    break;
                default:
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sort method");
            }
        } else {
            // 默认按更新时间排序
            pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "updateTime"));
        }

        if (username != null && !username.isEmpty()) {
            if (includeDeleted != null && includeDeleted) {
                questions = mongoQuestionRepository.findByUserIdAndIsHiddenFalseAndIsPostedTrue(username, pageable);
            } else {
                questions = mongoQuestionRepository.findByUserIdAndIsDeletedFalseAndIsHiddenFalseAndIsPostedTrue(username, pageable);
            }
        } else {
            if (includeDeleted != null && includeDeleted) {
                questions = mongoQuestionRepository.findByIsHiddenFalseAndIsPostedTrue(pageable);
            } else {
                questions = mongoQuestionRepository.findByIsDeletedFalseAndIsHiddenFalseAndIsPostedTrue(pageable);
            }
        }

        if (questions == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No questions found");
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
    public @Nullable QuestionEntity getQuestionById(Integer questionId) {

        return mongoQuestionRepository.findByBindIdAndIsDeletedFalse(questionId);
    }

    @Override
    public List<QuestionEntity> getQuestionByIds(List<Integer> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<QuestionEntity> questions = mongoQuestionRepository.findByBindIdInAndIsDeletedFalseAndIsPostedTrue(questionIds);
        if (questions == null || questions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No questions found for the provided IDs");
        }
        return questions;
    }

    @Override
    public MsgEntity<QuestionEntity> updateQuestion(Integer questionId, String title, String content,
                                                    Boolean isPost, Boolean isHide, Integer setReward, String userId) {
        // TODO: 没有处理setReward

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
            answerService.makeAnswerVisible(questionId, question.getIsPosted() & !question.getIsHidden() & !question.getIsDeleted());
        }

        if (isHide != null) {
            question.setIsHidden(isHide);
            answerService.makeAnswerVisible(questionId, question.getIsPosted() & !question.getIsHidden() & !question.getIsDeleted());
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

    @Override
    public MsgEntity<Page<QuestionEntity>> getMyDrafts(String userId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "updateTime"));
        Page<QuestionEntity> drafts = mongoQuestionRepository.findByUserIdAndIsPostedFalseAndIsHiddenFalseAndIsDeletedFalse(userId, pageable);
        
        if (drafts == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve drafts");
        }

        return new MsgEntity<>("SUCCESS", "Drafts retrieved successfully", drafts);
    }

    @Override
    @Async
    public void updateAnswerCount(Integer questionId, int delta) {
        QuestionEntity question = getQuestionById(questionId);
        if (question == null) {
            return;
        }

        // 更新回答数，确保不会小于0
        int newCount = Math.max(0, question.getAnswerCount() + delta);
        question.setAnswerCount(newCount);
        mongoQuestionRepository.save(question);
    }

    @Override
    @Async
    public void incrementViewCount(Integer questionId) {
        QuestionEntity question = getQuestionById(questionId);
        if (question == null) {
            return;
        }

        // 检查用户是否已经查看过这个问题
        UserBehaviorEntity viewQuery = new UserBehaviorEntity(questionId, MainType.QUESTION, question.getUserId(), BehaviorType.DO_READ);
        List<UserBehaviorEntity> viewBehaviors = userBehaviorService.findAllBehaviorRecords(viewQuery);
        
        // 如果用户还没有查看过这个问题，则增加浏览次数
        if (viewBehaviors.isEmpty()) {
            question.setViewCount(question.getViewCount() + 1);
            mongoQuestionRepository.save(question);
        }
    }

    @Override
    public MsgEntity<Page<QuestionEntity>> getAllQuestionsForAdmin(int page, int pageSize, Integer sortMethod) {
        Pageable pageable;
        Page<QuestionEntity> questions;

        if (sortMethod != null) {
            switch (sortMethod) {
                case 0: // 按更新时间排序
                    pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "updateTime"));
                    break;
                case 1: // 按热度排序
                    pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "viewCount"));
                    break;
                default:
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sort method");
            }
        } else {
            // 默认按更新时间排序
            pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "updateTime"));
        }

        // 获取所有问题，不限制可见性
        questions = mongoQuestionRepository.findAll(pageable);

        return new MsgEntity<>("SUCCESS", "All questions retrieved successfully", questions);
    }
}