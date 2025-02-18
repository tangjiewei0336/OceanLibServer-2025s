package com.oriole.ocean.service;

import com.alibaba.fastjson.JSONObject;
import com.oriole.ocean.common.enumerate.BehaviorExtraInfo;
import com.oriole.ocean.common.enumerate.BehaviorType;
import com.oriole.ocean.common.enumerate.EvaluateType;
import com.oriole.ocean.common.po.mongo.UserBehaviorEntity;
import com.oriole.ocean.common.service.UserBehaviorService;
import com.oriole.ocean.common.vo.BusinessException;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@DubboService
public class UserBehaviorServiceImpl implements UserBehaviorService {

    @Autowired
    private MongoTemplate mongoTemplate;

//    @Autowired
//    private NotifyServiceImpl notifyService;

    // 检查某用户针对特定对象（某文档/某便签等）的评价行为，并得出此次评价所需发生的全部评价行为
    // 对特定对象具有评论区时，也可处理对其评论区的评价行为
    // 全部评价行为释义：如用户对文档进行点赞操作，可能有点赞操作，还有可能同时具有取消点踩操作（如果用户已经点踩了的话），因此并非是单一的评价行为
    public List<EvaluateType> checkAndGetUserEvaluateBehavior(UserBehaviorEntity userBehavior, Boolean isCancel, Boolean isLike) {
        boolean isForComment = userBehavior.getExtraInfo(BehaviorExtraInfo.COMMENT_ID) != null;

        userBehavior.setBehaviorType(isForComment ? BehaviorType.DO_COMMENT_LIKE : BehaviorType.DO_LIKE);
        boolean hasLikeRecord = findBehaviorRecord(userBehavior) != null;

        userBehavior.setBehaviorType(isForComment ? BehaviorType.DO_COMMENT_DISLIKE : BehaviorType.DO_DISLIKE);
        boolean hasDislikeRecord = findBehaviorRecord(userBehavior) != null;

        //检查是否重复评价
        if (((isLike && hasLikeRecord) || (!isLike && hasDislikeRecord)) && !isCancel) {
            throw new BusinessException("-2", "Already recorded, Cannot repeat evaluation!");
        }
        //检查是否非法取消评价
        if (((isLike && !hasLikeRecord) || (!isLike && !hasDislikeRecord)) && isCancel) {
            throw new BusinessException("-4", "No recorded, Unable to cancel evaluation!");
        }
        List<EvaluateType> evaluates = new ArrayList<>();
        if (isLike) {
            if (!isCancel) {
                evaluates.add(EvaluateType.LIKE);
                if (hasDislikeRecord) {
                    evaluates.add(EvaluateType.CANCEL_DISLIKE);
                }
            } else {
                evaluates.add(EvaluateType.CANCEL_LIKE);
            }
        } else {
            if (!isCancel) {
                evaluates.add(EvaluateType.DISLIKE);
                if (hasLikeRecord) {
                    evaluates.add(EvaluateType.CANCEL_LIKE);
                }
            } else {
                evaluates.add(EvaluateType.CANCEL_DISLIKE);
            }
        }
        return evaluates;
    }

    // 将用户针对特定对象（某文档/某便签等）的评价行为存入数据库
    public void setUserEvaluateBehavior(UserBehaviorEntity userBehavior, List<EvaluateType> evaluates) {
        boolean isForComment = userBehavior.getExtraInfo(BehaviorExtraInfo.COMMENT_ID) != null;

        for (EvaluateType evaluate : evaluates) {
            switch (evaluate) {
                case CANCEL_LIKE:
                case LIKE:
                    userBehavior.setBehaviorType(isForComment ? BehaviorType.DO_COMMENT_LIKE : BehaviorType.DO_LIKE);
                    break;
                case CANCEL_DISLIKE:
                case DISLIKE:
                    userBehavior.setBehaviorType(isForComment ? BehaviorType.DO_COMMENT_DISLIKE : BehaviorType.DO_DISLIKE);
                    break;
            }
            switch (evaluate) {
                case CANCEL_LIKE:
                case CANCEL_DISLIKE:
                    deleteBehaviorRecord(userBehavior);
                    break;
                case LIKE:
                case DISLIKE:
                    setBehaviorRecord(userBehavior);
                    break;
            }
        }
    }

    public UserBehaviorEntity findBehaviorRecord(UserBehaviorEntity userBehavior) {
        return mongoTemplate.findOne(userBehavior.getQuery(), UserBehaviorEntity.class, "user_behavior");
    }

    public List<UserBehaviorEntity> findAllBehaviorRecords(UserBehaviorEntity userBehavior) {
        return mongoTemplate.find(userBehavior.getQuery(), UserBehaviorEntity.class, "user_behavior");
    }

    public List<UserBehaviorEntity> findAllBehaviorRecords(UserBehaviorEntity userBehavior, Integer minusDayNum) {
        Query query = userBehavior.getQuery();
        if (minusDayNum != -1) {
            ZonedDateTime minusDays = LocalDateTime.now().atZone(ZoneId.systemDefault()).minusDays(minusDayNum);
            query.addCriteria(Criteria.where("time").gte(Date.from(minusDays.toInstant())));
        }
        query.with(Sort.by(Sort.Order.desc("time")));
        return mongoTemplate.find(query, UserBehaviorEntity.class, "user_behavior");
    }

    public List<UserBehaviorEntity> findAllBehaviorRecords(UserBehaviorEntity userBehavior, Date startDate, Date endDate) {
        Query query = userBehavior.getQuery();
        Criteria criteria = new Criteria();
        criteria.andOperator(Criteria.where("time").gte(startDate), Criteria.where("time").lte(endDate));
        query.addCriteria(criteria);
        return mongoTemplate.find(query, UserBehaviorEntity.class, "user_behavior");
    }

    public void setBehaviorRecord(UserBehaviorEntity userBehavior) {
        mongoTemplate.save(userBehavior, "user_behavior");
        // 部分用户行为需要产生用户消息
//        notifyService.addNotifyByBehaviorRecord(userBehaviorEntity);
    }

    public void updateBehaviorRecordExtraInfo(UserBehaviorEntity userBehavior, BehaviorExtraInfo behaviorExtraInfo, Object value) {
        JSONObject extraInfo = new JSONObject();
        extraInfo.put(behaviorExtraInfo.toString(), value);
        Query query = userBehavior.getQuery();
        Update update = new Update();
        update.set("extraInfo", extraInfo);
        mongoTemplate.upsert(query, update, "user_behavior");

    }

    public void deleteBehaviorRecord(UserBehaviorEntity userBehavior) {
        Query query = userBehavior.getQuery();
        Update update = new Update();
        update.set("isCancel", true);
        mongoTemplate.upsert(query, update, "user_behavior");
    }

    public void deleteBehaviorRecordReal(UserBehaviorEntity userBehavior) {
        Query query = userBehavior.getQuery();
        mongoTemplate.remove(query, "user_behavior");
    }

}
