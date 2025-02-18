package com.oriole.ocean.common.po.mongo;

import com.alibaba.fastjson.JSONObject;
import com.oriole.ocean.common.enumerate.BehaviorExtraInfo;
import com.oriole.ocean.common.enumerate.BehaviorType;
import com.oriole.ocean.common.enumerate.MainType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Date;

@Data
public class UserBehaviorEntity implements java.io.Serializable {

    @Id
    private String id;
    @Field
    private Integer bindID;
    @Field
    private MainType type;
    @Field
    private String doUsername;
    @Field
    private BehaviorType behaviorType;
    @Field
    private JSONObject extraInfo;
    @Field
    private Date time;
    @Field
    private Boolean isCancel;

    public UserBehaviorEntity() {
        this.time = new Date();
        this.isCancel = false;
    }

    public UserBehaviorEntity(Integer bindID, MainType type, String doUsername, BehaviorType behaviorType) {
        this.bindID = bindID;
        this.type = type;
        this.doUsername = doUsername;
        this.behaviorType = behaviorType;
        this.time = new Date();
        this.isCancel = false;
    }

    public UserBehaviorEntity(String doUsername, BehaviorType behaviorType) {
        this.doUsername = doUsername;
        this.behaviorType = behaviorType;
        this.time = new Date();
        this.isCancel = false;
    }

    public Object getExtraInfo(BehaviorExtraInfo behaviorExtraInfo) {
        if (extraInfo == null) {
            return null;
        }else {
            return extraInfo.get(behaviorExtraInfo.toString());
        }
    }

    public void setExtraInfo(BehaviorExtraInfo behaviorExtraInfo, Object value) {
        if (extraInfo == null) {
            extraInfo = new JSONObject();
        }
        extraInfo.put(behaviorExtraInfo.toString(), value);
    }

    public JSONObject getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(JSONObject extraInfo) {
        this.extraInfo = extraInfo;
    }

    public Query getQuery() {
        Query query = new Query();
        if (bindID != null) {
            query.addCriteria(Criteria.where("bindID").is(bindID));
        }
        if (type != null) {
            query.addCriteria(Criteria.where("type").is(type));
        }
        if (doUsername != null) {
            query.addCriteria(Criteria.where("doUsername").is(doUsername));
        }
        if (behaviorType != null) {
            query.addCriteria(Criteria.where("behaviorType").is(behaviorType));
        }
        if (extraInfo != null) {
            query.addCriteria(Criteria.where("extraInfo").is(extraInfo));
        }
        query.addCriteria(Criteria.where("isCancel").is(isCancel));
        return query;
    }
}