package com.oriole.ocean.common.po.mongo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.istack.internal.NotNull;
import io.swagger.annotations.ApiModelProperty;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Answer
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-04-19T15:37:48.929758+08:00[Asia/Shanghai]")

public class AnswerEntity {
  @JsonProperty("answerId")
  private String answerId;

  @JsonProperty("questionId")
  private String questionId;

  @JsonProperty("content")
  private String content;

  @JsonProperty("userId")
  private String userId;

  @JsonProperty("createdAt")
  @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime createdAt;

  public AnswerEntity answerId(String answerId) {
    this.answerId = answerId;
    return this;
  }

  /**
   * Get answerId
   * @return answerId
  */
  @ApiModelProperty(required = true, value = "")
  @NotNull


  public String getAnswerId() {
    return answerId;
  }

  public void setAnswerId(String answerId) {
    this.answerId = answerId;
  }

  public AnswerEntity questionId(String questionId) {
    this.questionId = questionId;
    return this;
  }

  /**
   * Get questionId
   * @return questionId
  */
  @ApiModelProperty(required = true, value = "")
  @NotNull


  public String getQuestionId() {
    return questionId;
  }

  public void setQuestionId(String questionId) {
    this.questionId = questionId;
  }

  public AnswerEntity content(String content) {
    this.content = content;
    return this;
  }

  /**
   * Get content
   * @return content
  */
  @ApiModelProperty(required = true, value = "")
  @NotNull


  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public AnswerEntity userId(String userId) {
    this.userId = userId;
    return this;
  }

  /**
   * Get userId
   * @return userId
  */
  @ApiModelProperty(required = true, value = "")
  @NotNull


  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public AnswerEntity createdAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   * Get createdAt
   * @return createdAt
  */
  @ApiModelProperty(required = true, value = "")
  @NotNull

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AnswerEntity answerEntity = (AnswerEntity) o;
    return Objects.equals(this.answerId, answerEntity.answerId) &&
        Objects.equals(this.questionId, answerEntity.questionId) &&
        Objects.equals(this.content, answerEntity.content) &&
        Objects.equals(this.userId, answerEntity.userId) &&
        Objects.equals(this.createdAt, answerEntity.createdAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(answerId, questionId, content, userId, createdAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Answer {\n");
    
    sb.append("    answerId: ").append(toIndentedString(answerId)).append("\n");
    sb.append("    questionId: ").append(toIndentedString(questionId)).append("\n");
    sb.append("    content: ").append(toIndentedString(content)).append("\n");
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

