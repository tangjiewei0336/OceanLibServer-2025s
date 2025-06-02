package com.oriole.ocean.common.po.es;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.Date;
import java.util.List;

@Data
@Document(indexName = "question_entity")
public class QuestionEntity {
    private Integer bindId;
    private String userId;
    private String title;
    private String content;
    private Date createTime;
    private Date updateTime;
    private Boolean isDeleted = false;
    private Boolean isPosted = false;
    private Boolean isHidden = false;
    private Integer rewardPoints = 0;
    private Integer answerCount = 0;
    private Integer viewCount = 0;
    private List<String> tagIds;
    private List<String> attachmentIds;
    private boolean isLiked = false;
}