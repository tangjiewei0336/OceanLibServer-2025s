package com.oriole.ocean.controller;

import com.oriole.ocean.common.auth.AuthUser;
import com.oriole.ocean.common.enumerate.BehaviorType;
import com.oriole.ocean.common.enumerate.EvaluateType;
import com.oriole.ocean.common.enumerate.MainType;
import com.oriole.ocean.common.enumerate.NotifyAction;
import com.oriole.ocean.common.enumerate.NotifyType;
import com.oriole.ocean.common.po.mongo.AnswerEntity;
import com.oriole.ocean.common.po.mongo.QuestionEntity;
import com.oriole.ocean.common.po.mongo.UserBehaviorEntity;
import com.oriole.ocean.common.po.mysql.NotifyEntity;
import com.oriole.ocean.common.service.NotifyService;
import com.oriole.ocean.common.service.UserBehaviorService;
import com.oriole.ocean.common.vo.AuthUserEntity;
import com.oriole.ocean.common.vo.BusinessException;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.service.AnswerService;
import com.oriole.ocean.service.QuestionService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/qaService/like")
public class LikeController {

    @Autowired
    private AnswerService answerService;

    @Autowired
    private QuestionService questionService;

    @DubboReference
    private UserBehaviorService userBehaviorService;

    @DubboReference
    private NotifyService notifyService;

    @Autowired
    private MongoTemplate mongoTemplate;


    @ApiOperation(value = "评价一个问题", notes = "isLike参数为评价操作的分类（true表示为点赞操作，false表示为点踩操作），isCancel为是否为取消评价操作。如isLike为false，isCancel为true，则表示取消点踩操作")
    @PostMapping(value = "/evaluateQuestion", produces = {"application/json"})
    public MsgEntity<Object> evaluateQuestion(@AuthUser AuthUserEntity authUser, @ApiParam(value = "问题ID", required = true) @RequestParam Integer questionId, @ApiParam(value = "是否为取消操作", required = true) @RequestParam Boolean isCancel, @ApiParam(value = "是否为点赞操作", required = true) @RequestParam Boolean isLike) {

        // 获取问题信息
        QuestionEntity question = questionService.getQuestionById(questionId);
        if (question == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found");
        }

        // 检查是否是在评价自己的问题
        if (authUser.getUsername().equals(question.getUserId())) {
            return new MsgEntity<>("-1", "ERRSELEV", "不能评价自己的问题。");
        }

        // 检查是否已经点赞
        if (isLike && !isCancel) {
            UserBehaviorEntity existingLike = userBehaviorService.findBehaviorRecord(new UserBehaviorEntity(questionId, MainType.QUESTION, authUser.getUsername(), BehaviorType.DO_LIKE));
            if (existingLike != null && !existingLike.getIsCancel()) {
                return new MsgEntity<>("-1","ERRDUP", "你已经赞过了。");
            }
        }

        // 检查是否已经点踩
        if (!isLike && !isCancel) {
            UserBehaviorEntity existingDislike = userBehaviorService.findBehaviorRecord(new UserBehaviorEntity(questionId, MainType.QUESTION, authUser.getUsername(), BehaviorType.DO_DISLIKE));
            if (existingDislike != null && !existingDislike.getIsCancel()) {
                return new MsgEntity<>("-1","ERRDUP", "你已踩过了。");
            }
        }

        // 检查是否可以取消点赞
        if (isLike && isCancel) {
            UserBehaviorEntity existingLike = userBehaviorService.findBehaviorRecord(new UserBehaviorEntity(questionId, MainType.QUESTION, authUser.getUsername(), BehaviorType.DO_LIKE));
            if (existingLike == null || existingLike.getIsCancel()) {
                return new MsgEntity<>("-1","ERRNOBE", "你还没赞呢。");
            }
        }

        // 检查是否可以取消点踩
        if (!isLike && isCancel) {
            UserBehaviorEntity existingDislike = userBehaviorService.findBehaviorRecord(new UserBehaviorEntity(questionId, MainType.QUESTION, authUser.getUsername(), BehaviorType.DO_DISLIKE));
            if (existingDislike == null || existingDislike.getIsCancel()) {
                return new MsgEntity<>("-1","ERRNOBE", "你还没踩呢。");
            }
        }

        // 添加用户行为
        UserBehaviorEntity userBehaviorEntity = new UserBehaviorEntity(questionId, MainType.QUESTION, authUser.getUsername(), null);
        try {
            List<EvaluateType> evaluates = userBehaviorService.checkAndGetUserEvaluateBehavior(userBehaviorEntity, isCancel, isLike);
            userBehaviorService.setUserEvaluateBehavior(userBehaviorEntity, evaluates);

            // 更新问题的点赞/点踩数
            for (EvaluateType evaluate : evaluates) {
                Query query = Query.query(Criteria.where("bind_id").is(questionId));
                Update update = new Update();

                switch (evaluate) {
                    case CANCEL_LIKE:
                        update.inc("like_count", -1);
                        break;
                    case LIKE:
                        update.inc("like_count", 1);
                        break;
                    case CANCEL_DISLIKE:
                        update.inc("dislike_count", -1);
                        break;
                    case DISLIKE:
                        update.inc("dislike_count", 1);
                        break;
                }

                mongoTemplate.updateFirst(query, update, QuestionEntity.class);
            }
        } catch (BusinessException businessException) {
            businessException.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, " Already recorded, Cannot repeat evaluation!");
        }

        // 只在点赞时生成通知
        if (isLike && !isCancel) {
            NotifyEntity notifyEntity = new NotifyEntity(NotifyType.REMIND, question.getUserId());
            notifyEntity.setAction(NotifyAction.LIKE);
            notifyEntity.setTargetIDAndType(String.valueOf(questionId), MainType.QUESTION);
            notifyEntity.setUserBehaviorID(userBehaviorEntity.getId());
            notifyService.addNotify(notifyEntity);
        }

        // 获取更新后的问题信息
        question = questionService.getQuestionById(questionId);
        Map<String, Integer> result = new HashMap<>();
        result.put("likeCount", question.getLikeCount());
        result.put("dislikeCount", question.getDislikeCount());
        result.put("netCount", question.getLikeCount() - question.getDislikeCount());

        return new MsgEntity<>("SUCCESS", "1", result);
    }

    @ApiOperation(value = "评价一个回答", notes = "isLike参数为评价操作的分类（true表示为点赞操作，false表示为点踩操作），isCancel为是否为取消评价操作。如isLike为false，isCancel为true，则表示取消点踩操作")
    @PostMapping(value = "/evaluateAnswer", produces = {"application/json"})
    public MsgEntity<Object> evaluateAnswer(@AuthUser AuthUserEntity authUser, @ApiParam(value = "回答ID", required = true) @RequestParam Integer answerId, @ApiParam(value = "是否为取消操作", required = true) @RequestParam Boolean isCancel, @ApiParam(value = "是否为点赞操作", required = true) @RequestParam Boolean isLike) {

        // 获取回答信息
        AnswerEntity answer = answerService.getAnswerById(answerId, authUser.getUsername());
        if (answer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Answer not found");
        }

        // 检查是否是在评价自己的回答
        if (authUser.getUsername().equals(answer.getUserId())) {
            return new MsgEntity<>("-1", "ERROR", "不能给自己的回答点赞或者点踩。");
        }

        // 检查是否已经点赞
        if (isLike && !isCancel) {
            UserBehaviorEntity existingLike = userBehaviorService.findBehaviorRecord(new UserBehaviorEntity(answerId, MainType.ANSWER, authUser.getUsername(), BehaviorType.DO_LIKE));
            if (existingLike != null && !existingLike.getIsCancel()) {
                return new MsgEntity<>("-1","ERRDUP", "你已经赞过了。");
            }
        }

        // 检查是否已经点踩
        if (!isLike && !isCancel) {
            UserBehaviorEntity existingDislike = userBehaviorService.findBehaviorRecord(new UserBehaviorEntity(answerId, MainType.ANSWER, authUser.getUsername(), BehaviorType.DO_DISLIKE));
            if (existingDislike != null && !existingDislike.getIsCancel()) {
                return new MsgEntity<>("-1","ERRDUP", "你已经踩过了。");
            }
        }

        // 添加用户行为
        UserBehaviorEntity userBehaviorEntity = new UserBehaviorEntity(answerId, MainType.ANSWER, authUser.getUsername(), null);
        List<EvaluateType> evaluates = userBehaviorService.checkAndGetUserEvaluateBehavior(userBehaviorEntity, isCancel, isLike);
        userBehaviorService.setUserEvaluateBehavior(userBehaviorEntity, evaluates);

        // 更新回答的点赞/点踩数
        for (EvaluateType evaluate : evaluates) {
            Query query = Query.query(Criteria.where("_id").is(answerId));
            Update update = new Update();

            switch (evaluate) {
                case CANCEL_LIKE:

                    UserBehaviorEntity existingLike = userBehaviorService.findBehaviorRecord(new UserBehaviorEntity(answerId, MainType.ANSWER, authUser.getUsername(), BehaviorType.DO_LIKE));
                    if (!(existingLike == null)) {
                        userBehaviorService.deleteBehaviorRecord(existingLike);
                        update.inc("like_count", -1);
                    }
                    break;
                case LIKE:
                    update.inc("like_count", 1);
                    break;
                case CANCEL_DISLIKE:
                    UserBehaviorEntity existingDislike = userBehaviorService.findBehaviorRecord(new UserBehaviorEntity(answerId, MainType.ANSWER, authUser.getUsername(), BehaviorType.DO_DISLIKE));
                    if (!(existingDislike == null)) {
                        update.inc("dislike_count", -1);
                        userBehaviorService.deleteBehaviorRecord(existingDislike);
                    }
                    break;
                case DISLIKE:
                    update.inc("dislike_count", 1);
                    break;
            }

            mongoTemplate.updateFirst(query, update, AnswerEntity.class);
        }

        // 只在点赞时生成通知
        if (isLike && !isCancel) {
            NotifyEntity notifyEntity = new NotifyEntity(NotifyType.REMIND, answer.getUserId());
            notifyEntity.setAction(NotifyAction.LIKE);
            notifyEntity.setTargetIDAndType(String.valueOf(answerId), MainType.ANSWER);
            notifyEntity.setUserBehaviorID(userBehaviorEntity.getId());
            notifyService.addNotify(notifyEntity);
        }

        // 获取更新后的回答信息
        answer = answerService.getAnswerById(answerId, authUser.getUsername());
        Map<String, Integer> result = new HashMap<>();
        result.put("likeCount", answer.getLikeCount());
        result.put("dislikeCount", answer.getDislikeCount());
        result.put("netCount", answer.getLikeCount() - answer.getDislikeCount());

        return new MsgEntity<>("SUCCESS", "1", result);
    }
}
