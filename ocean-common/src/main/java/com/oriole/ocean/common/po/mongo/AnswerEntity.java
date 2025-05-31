package com.oriole.ocean.common.po.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Data
@Document(collection = "answers")
public class AnswerEntity {
  @Id
  private Integer id;

  @Field("question_id")
  private Integer questionId;

  @Field("user_id")
  private String userId;

  private String content;

  @Field("create_time")
  private Date createTime;

  @Field("update_time")
  private Date updateTime;

  @Field("is_deleted")
  private Boolean isDeleted = false;

  @Field("like_count")
  private Integer likeCount = 0;

  @Field("dislike_count")
  private Integer dislikeCount = 0;

  @Field("comment_count")
  private Integer commentCount = 0;

  // 非持久化字段

  private QuestionEntity question;
}