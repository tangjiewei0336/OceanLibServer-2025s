package com.oriole.ocean.repository;

import com.oriole.ocean.common.po.mongo.AnswerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AnswerRepository extends MongoRepository<AnswerEntity, String> {
    Page<AnswerEntity> findByQuestionIdAndIsDeletedFalse(Integer questionId, Pageable pageable);

    AnswerEntity findByIdAndIsDeletedFalse(Integer id);

    Page<AnswerEntity> findByUserIdAndIsDeletedFalse(String userId, Pageable pageable);

    List<AnswerEntity> findByUserIdAndIsDeletedFalse(String userId);
}