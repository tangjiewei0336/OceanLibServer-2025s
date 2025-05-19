package com.oriole.ocean.common.po.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

@Data
@Document(collection = "questions")
public class QuestionEntity {
    @Id
    private Integer id;

    @Field("user_id")
    private String userId;

    private String title;
    private String content;

    @Field("create_time")
    private Date createTime;

    @Field("update_time")
    private Date updateTime;

    @Field("is_deleted")
    private Boolean isDeleted = false;

    @Field("is_posted")
    private Boolean isPosted = false;

    @Field("is_hidden")
    private Boolean isHidden = false;

    @Field("reward_points")
    private Integer rewardPoints = 0;

    @Field("answer_count")
    private Integer answerCount = 0;

    @Field("view_count")
    private Integer viewCount = 0;

    @Field("tag_ids")
    private List<String> tagIds;

    @Field("attachment_ids")
    private List<String> attachmentIds;

    // 非持久化字段
    private boolean isLiked = false;
}