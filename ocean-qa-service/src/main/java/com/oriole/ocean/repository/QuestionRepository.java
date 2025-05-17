package com.oriole.ocean.repository;

import com.oriole.ocean.common.po.mongo.QuestionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QuestionRepository extends MongoRepository<QuestionEntity, String> {
    Page<QuestionEntity> findByIsDeletedFalse(Pageable pageable);

    Page<QuestionEntity> findByUserIdAndIsDeletedFalse(String userId, Pageable pageable);

    QuestionEntity findByIdAndIsDeletedFalse(Integer id);

    long countByUserIdAndIsDeletedFalse(String userId);

    List<QuestionEntity> findByIdInAndIsDeletedFalse(List<Integer> questionIds);
}
