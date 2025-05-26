package com.oriole.ocean.common.po.es;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

@Data
@org.springframework.data.elasticsearch.annotations.Document(indexName = "question_entity")
public class QuestionEntity {

    private Integer bindId;

    @org.springframework.data.elasticsearch.annotations.Field(type = FieldType.Text)
    private String userId;

    @org.springframework.data.elasticsearch.annotations.Field(type = FieldType.Text)
    private String title;

    @org.springframework.data.elasticsearch.annotations.Field(type = FieldType.Text)
    private String content;

    private Date createTime;

    private Date updateTime;

    @org.springframework.data.elasticsearch.annotations.Field(type = FieldType.Boolean)
    private Boolean isDeleted = false;

    private Boolean isPosted = false;

//    @org.springframework.data.elasticsearch.annotations.Field(type = FieldType.Boolean)
    private Boolean isHidden = false;

    private Integer rewardPoints = 0;

//    @org.springframework.data.elasticsearch.annotations.Field(type = FieldType.Integer)
    private Integer answerCount = 0;

//    @org.springframework.data.elasticsearch.annotations.Field(type = FieldType.Integer)
    private Integer viewCount = 0;

//    @org.springframework.data.elasticsearch.annotations.Field(type = FieldType.Keyword)
    private List<String> tagIds;

    private List<String> attachmentIds;

    // 非持久化字段
    private boolean isLiked = false;
}