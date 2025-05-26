package com.oriole.ocean.repository;

import com.oriole.ocean.common.po.mongo.QuestionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoQuestionRepository extends MongoRepository<QuestionEntity, String> {
    Page<QuestionEntity> findByIsDeletedFalse(Pageable pageable);

    Page<QuestionEntity> findByUserIdAndIsDeletedFalse(String userId, Pageable pageable);

    QuestionEntity findByBindIdAndIsDeletedFalse(Integer id);

    long countByUserIdAndIsDeletedFalse(String userId);

    List<QuestionEntity> findByBindIdInAndIsDeletedFalse(List<Integer> questionIds);

    Page<QuestionEntity> findByUserId(String username, Pageable pageable);
}
