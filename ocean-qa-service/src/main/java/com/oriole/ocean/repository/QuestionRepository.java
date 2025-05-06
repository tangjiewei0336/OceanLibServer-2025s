package com.oriole.ocean.repository;

import com.oriole.ocean.common.po.mongo.QuestionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QuestionRepository extends MongoRepository<QuestionEntity, String> {
    Page<QuestionEntity> findByIsDeletedFalse(Pageable pageable);

    Page<QuestionEntity> findByUserIdAndIsDeletedFalse(String userId, Pageable pageable);

    QuestionEntity findByIdAndIsDeletedFalse(String id);

    long countByUserIdAndIsDeletedFalse(String userId);
}