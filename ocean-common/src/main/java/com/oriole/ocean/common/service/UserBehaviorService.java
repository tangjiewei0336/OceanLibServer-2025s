package com.oriole.ocean.common.service;

import com.alibaba.fastjson.JSONObject;
import com.oriole.ocean.common.enumerate.BehaviorExtraInfo;
import com.oriole.ocean.common.enumerate.EvaluateType;
import com.oriole.ocean.common.po.mongo.UserBehaviorEntity;

import java.util.Date;
import java.util.List;

public interface UserBehaviorService {

    List<EvaluateType> checkAndGetUserEvaluateBehavior(UserBehaviorEntity userBehavior, Boolean isCancel, Boolean isLike);

    // 将用户针对特定对象（某文档/某便签等）的评价行为存入数据库
    void setUserEvaluateBehavior(UserBehaviorEntity userBehavior, List<EvaluateType> evaluates);

    UserBehaviorEntity findBehaviorRecord(UserBehaviorEntity userBehavior);

    List<UserBehaviorEntity> findAllBehaviorRecords(UserBehaviorEntity userBehavior);

    List<UserBehaviorEntity> findAllBehaviorRecords(UserBehaviorEntity userBehavior, Integer minusDayNum);

    List<UserBehaviorEntity> findAllBehaviorRecords(UserBehaviorEntity userBehavior, Date startDate, Date endDate);

    void setBehaviorRecord(UserBehaviorEntity UserBehavior);

    void updateBehaviorRecordExtraInfo(UserBehaviorEntity userBehavior, BehaviorExtraInfo behaviorExtraInfo, Object value);


    void deleteBehaviorRecord(UserBehaviorEntity userBehavior);

    void deleteBehaviorRecordReal(UserBehaviorEntity userBehavior);

}
