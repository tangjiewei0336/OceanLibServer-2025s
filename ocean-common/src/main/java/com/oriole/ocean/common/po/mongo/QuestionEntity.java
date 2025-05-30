package com.oriole.ocean.common.po.mongo;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

@Data
@Document(collection = "questions")
//@org.springframework.data.elasticsearch.annotations.Document(indexName = "questions")
public class QuestionEntity {
    @Id
    private ObjectId id;

    @Field("bind_id")
    private Integer bindId;

    @Field("user_id")
//    @org.springframework.data.elasticsearch.annotations.Field(type = FieldType.Text)
    private String userId;

//    @org.springframework.data.elasticsearch.annotations.Field(type = FieldType.Text)
    private String title;
//    @org.springframework.data.elasticsearch.annotations.Field(type = FieldType.Text)
    private String content;

    @Field("create_time")
    private Date createTime;

    @Field("update_time")
    private Date updateTime;

    @Field("is_deleted")
//    @org.springframework.data.elasticsearch.annotations.Field(type = FieldType.Boolean)
    private Boolean isDeleted = false;

    @Field("is_posted")
    private Boolean isPosted = false;

    @Field("is_hidden")
//    @org.springframework.data.elasticsearch.annotations.Field(type = FieldType.Boolean)
    private Boolean isHidden = false;

    @Field("reward_points")
    private Integer rewardPoints = 0;

    @Field("answer_count")
//    @org.springframework.data.elasticsearch.annotations.Field(type = FieldType.Integer)
    private Integer answerCount = 0;

    @Field("view_count")
//    @org.springframework.data.elasticsearch.annotations.Field(type = FieldType.Integer)
    private Integer viewCount = 0;

    @Field("tag_ids")
//    @org.springframework.data.elasticsearch.annotations.Field(type = FieldType.Keyword)
    private List<String> tagIds;

    @Field("attachment_ids")
    private List<String> attachmentIds;

    // 非持久化字段
    private boolean isLiked = false;
}