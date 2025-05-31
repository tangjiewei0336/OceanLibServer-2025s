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
    public MsgEntity<Map<String, Integer>> evaluateQuestion(
            @AuthUser AuthUserEntity authUser,
            @ApiParam(value = "问题ID", required = true) @RequestParam Integer questionId,
            @ApiParam(value = "是否为取消操作", required = true) @RequestParam Boolean isCancel,
            @ApiParam(value = "是否为点赞操作", required = true) @RequestParam Boolean isLike) {

        // 获取问题信息
        QuestionEntity question = questionService.getQuestionById(questionId);
        if (question == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found");
        }

        // 检查是否是在评价自己的问题
        if (authUser.getUsername().equals(question.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot evaluate your own question");
        }

        // 检查是否已经点赞
        if (isLike && !isCancel) {
            Query query = Query.query(Criteria.where("bindID").is(questionId)
                    .and("type").is(MainType.QUESTION)
                    .and("doUsername").is(authUser.getUsername())
                    .and("behaviorType").is(BehaviorType.DO_LIKE));
            UserBehaviorEntity existingLike = mongoTemplate.findOne(query, UserBehaviorEntity.class);
            if (existingLike != null && !existingLike.getIsCancel()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already liked this question");
            }
        }

        // 检查是否已经点踩
        if (!isLike && !isCancel) {
            Query query = Query.query(Criteria.where("bindID").is(questionId)
                    .and("type").is(MainType.QUESTION)
                    .and("doUsername").is(authUser.getUsername())
                    .and("behaviorType").is(BehaviorType.DO_DISLIKE));
            UserBehaviorEntity existingDislike = mongoTemplate.findOne(query, UserBehaviorEntity.class);
            if (existingDislike != null && !existingDislike.getIsCancel()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already disliked this question");
            }
        }

        // 检查是否可以取消点赞
        if (isLike && isCancel) {
            Query query = Query.query(Criteria.where("bindID").is(questionId)
                    .and("type").is(MainType.QUESTION)
                    .and("doUsername").is(authUser.getUsername())
                    .and("behaviorType").is(BehaviorType.DO_LIKE));
            UserBehaviorEntity existingLike = mongoTemplate.findOne(query, UserBehaviorEntity.class);
            if (existingLike == null || existingLike.getIsCancel()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have not liked this question yet");
            }
        }

        // 检查是否可以取消点踩
        if (!isLike && isCancel) {
            Query query = Query.query(Criteria.where("bindID").is(questionId)
                    .and("type").is(MainType.QUESTION)
                    .and("doUsername").is(authUser.getUsername())
                    .and("behaviorType").is(BehaviorType.DO_DISLIKE));
            UserBehaviorEntity existingDislike = mongoTemplate.findOne(query, UserBehaviorEntity.class);
            if (existingDislike == null || existingDislike.getIsCancel()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have not disliked this question yet");
            }
        }

        // 添加用户行为
        UserBehaviorEntity userBehaviorEntity = new UserBehaviorEntity(questionId, MainType.QUESTION, authUser.getUsername(), null);
        try {
            List<EvaluateType> evaluates = userBehaviorService.checkAndGetUserEvaluateBehavior(userBehaviorEntity, isCancel, isLike);
            userBehaviorService.setUserEvaluateBehavior(userBehaviorEntity, evaluates);

            // 更新问题的点赞/点踩数
            for (EvaluateType evaluate : evaluates) {
                Query query = Query.query(Criteria.where("_id").is(questionId));
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
        result.put("like_count", question.getLikeCount());
        result.put("dislike_count", question.getDislikeCount());
        result.put("net_count", question.getLikeCount() - question.getDislikeCount());

        return new MsgEntity<>("SUCCESS", "1", result);
    }

    @ApiOperation(value = "评价一个回答", notes = "isLike参数为评价操作的分类（true表示为点赞操作，false表示为点踩操作），isCancel为是否为取消评价操作。如isLike为false，isCancel为true，则表示取消点踩操作")
    @PostMapping(value = "/evaluateAnswer", produces = {"application/json"})
    public MsgEntity<Object> evaluateAnswer(
            @AuthUser AuthUserEntity authUser,
            @ApiParam(value = "回答ID", required = true) @RequestParam Integer answerId,
            @ApiParam(value = "是否为取消操作", required = true) @RequestParam Boolean isCancel,
            @ApiParam(value = "是否为点赞操作", required = true) @RequestParam Boolean isLike) {

        // 获取回答信息
        AnswerEntity answer = answerService.getAnswerById(answerId);
        if (answer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Answer not found");
        }

        // 检查是否是在评价自己的回答
        if (authUser.getUsername().equals(answer.getUserId())) {
            return new MsgEntity<>("-1","ERROR","不能给自己的回答点赞或者点踩。");
        }

        // 检查是否已经点赞
        if (isLike && !isCancel) {
            Query query = Query.query(Criteria.where("_id").is(answerId)
                    .and("type").is(MainType.ANSWER)
                    .and("doUsername").is(authUser.getUsername())
                    .and("behaviorType").is(BehaviorType.DO_LIKE));
            UserBehaviorEntity existingLike = mongoTemplate.findOne(query, UserBehaviorEntity.class);
            if (existingLike != null && !existingLike.getIsCancel()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already liked this answer");
            }
        }

        // 检查是否已经点踩
        if (!isLike && !isCancel) {
            Query query = Query.query(Criteria.where("_id").is(answerId)
                    .and("type").is(MainType.ANSWER)
                    .and("doUsername").is(authUser.getUsername())
                    .and("behaviorType").is(BehaviorType.DO_DISLIKE));
            UserBehaviorEntity existingDislike = mongoTemplate.findOne(query, UserBehaviorEntity.class);
            if (existingDislike != null && !existingDislike.getIsCancel()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already disliked this answer");
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

                    Query query_cancel_like = Query.query(Criteria.where("_id").is(answerId)
                            .and("type").is(MainType.ANSWER)
                            .and("doUsername").is(authUser.getUsername())
                            .and("behaviorType").is(BehaviorType.DO_LIKE));
                    UserBehaviorEntity existingLike = mongoTemplate.findOne(query_cancel_like, UserBehaviorEntity.class);
                    if (!(existingLike == null || existingLike.getIsCancel())) {
                        userBehaviorService.deleteBehaviorRecord(existingLike);
                        update.inc("like_count", -1);
                    }
                    break;
                case LIKE:
                    update.inc("like_count", 1);
                    break;
                case CANCEL_DISLIKE:
                    Query cancel_dislike_query = Query.query(Criteria.where("_id").is(answerId)
                            .and("type").is(MainType.ANSWER)
                            .and("doUsername").is(authUser.getUsername())
                            .and("behaviorType").is(BehaviorType.DO_DISLIKE));
                    UserBehaviorEntity existingDislike = mongoTemplate.findOne(cancel_dislike_query, UserBehaviorEntity.class);
                    if (!(existingDislike == null || existingDislike.getIsCancel())) {
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
        answer = answerService.getAnswerById(answerId);
        Map<String, Integer> result = new HashMap<>();
        result.put("like_count", answer.getLikeCount());
        result.put("dislike_count", answer.getDislikeCount());
        result.put("net_count", answer.getLikeCount() - answer.getDislikeCount());

        return new MsgEntity<>("SUCCESS", "1", result);
    }
}
