package com.oriole.ocean.service;

import com.oriole.ocean.common.po.mongo.QuestionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class DataSyncService {

    private static final Logger logger = LoggerFactory.getLogger(DataSyncService.class);


    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @PostConstruct
    @Scheduled(cron = "0 0 * * * ?") // 每小时第0分钟执行一次
    public void syncData() {
        logger.info("开始从 MongoDB 同步数据到 Elasticsearch...");

        List<QuestionEntity> allQuestions = mongoTemplate.findAll(QuestionEntity.class);
        logger.info("从 MongoDB 查询到 {} 条数据", allQuestions.size());

        List<com.oriole.ocean.common.po.es.QuestionEntity> exportList = new ArrayList<>();

        for (QuestionEntity question : allQuestions) {
            com.oriole.ocean.common.po.es.QuestionEntity exportQuestion = new com.oriole.ocean.common.po.es.QuestionEntity();

            exportQuestion.setBindId(question.getBindId());
            exportQuestion.setUserId(question.getUserId());
            exportQuestion.setTitle(question.getTitle());
            exportQuestion.setContent(question.getContent());
            exportQuestion.setCreateTime(question.getCreateTime());
            exportQuestion.setUpdateTime(question.getUpdateTime());
            exportQuestion.setIsDeleted(question.getIsDeleted());
            exportQuestion.setIsPosted(question.getIsPosted());
            exportQuestion.setIsHidden(question.getIsHidden());
            exportQuestion.setRewardPoints(question.getRewardPoints());
            exportQuestion.setAnswerCount(question.getAnswerCount());
            exportQuestion.setViewCount(question.getViewCount());
            exportQuestion.setTagIds(question.getTagIds());
            exportQuestion.setAttachmentIds(question.getAttachmentIds());

            // 非持久化字段
            exportQuestion.setLiked(false); // 默认不喜欢

            exportList.add(exportQuestion);
        }
        try {
            elasticsearchRestTemplate.save(exportList);
        } catch (org.springframework.dao.DataAccessResourceFailureException e) {
            if (!Objects.requireNonNull(e.getMessage()).contains("200 OK")) {
                logger.error("同步数据到 Elasticsearch 失败", e);
                throw e; // 其他异常抛出
            } else {
                logger.info("成功同步 {} 条数据到 Elasticsearch", exportList.size());
            }
        }


    }
}
