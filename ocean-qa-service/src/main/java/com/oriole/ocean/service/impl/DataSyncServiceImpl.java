package com.oriole.ocean.service.impl;

import com.oriole.ocean.common.po.mongo.QuestionEntity;
import com.oriole.ocean.service.DataSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class DataSyncServiceImpl implements DataSyncService {

    private static final Logger logger = LoggerFactory.getLogger(DataSyncServiceImpl.class);

    @Value("${data.sync.debug:true}")
    private boolean debugMode;

    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    private LocalDateTime lastSyncTime;

    @PostConstruct
    public void init() {
        lastSyncTime = LocalDateTime.now();
        if (debugMode) {
            logger.info("数据同步服务以调试模式启动");
            syncData();
        }
    }

    @Scheduled(cron = "0 0 * * * ?") // 每小时第0分钟执行一次
    public void syncData() {
        logger.info("开始从 MongoDB 同步数据到 Elasticsearch...");
        
        List<QuestionEntity> questions;
        if (debugMode) {
            // 调试模式：全量同步
            questions = mongoTemplate.findAll(QuestionEntity.class);
            logger.info("调试模式：从 MongoDB 全量查询到 {} 条数据", questions.size());
        } else {
            // 正常模式：增量同步
            Query query = new Query(Criteria.where("updateTime").gt(lastSyncTime));
            questions = mongoTemplate.find(query, QuestionEntity.class);
            logger.info("从 MongoDB 增量查询到 {} 条数据", questions.size());
        }

        if (questions.isEmpty()) {
            logger.info("没有需要同步的数据");
            return;
        }

        List<com.oriole.ocean.common.po.es.QuestionEntity> exportList = new ArrayList<>();

        for (QuestionEntity question : questions) {
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
            if (debugMode) {
                // 调试模式：先清空ES数据
                IndexOperations indexOps = elasticsearchRestTemplate.indexOps(com.oriole.ocean.common.po.es.QuestionEntity.class);
                if (indexOps.exists()) {
                    indexOps.delete();
                    logger.info("调试模式：已清空 Elasticsearch 索引");
                }
            }
            
            elasticsearchRestTemplate.save(exportList);
            lastSyncTime = LocalDateTime.now();
            
            logger.info("成功同步 {} 条数据到 Elasticsearch", exportList.size());
        } catch (org.springframework.dao.DataAccessResourceFailureException e) {
            if (!Objects.requireNonNull(e.getMessage()).contains("200 OK")) {
                logger.error("同步数据到 Elasticsearch 失败", e);
                throw e; // 其他异常抛出
            } else {
                logger.info("成功同步 {} 条数据到 Elasticsearch", exportList.size());
            }
        }
    }

    /**
     * 手动触发全量同步（仅用于调试）
     */
    public void forceFullSync() {
        if (!debugMode) {
            logger.warn("非调试模式下不允许强制全量同步");
            return;
        }
        
        logger.info("开始强制全量同步...");
        debugMode = true;
        try {
            syncData();
        } finally {
            debugMode = false;
        }
    }
}
 