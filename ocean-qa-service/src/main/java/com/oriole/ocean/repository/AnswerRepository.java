package com.oriole.ocean.repository;

import com.oriole.ocean.common.po.mongo.AnswerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import javax.validation.Valid;
import java.util.List;

public interface AnswerRepository extends MongoRepository<AnswerEntity, String> {
    Page<AnswerEntity> findByQuestionIdAndIsDeletedFalseAndQuestionVisibleTrue(Integer questionId, Pageable pageable);

    AnswerEntity findByIdAndIsDeletedFalseAndQuestionVisibleTrue(Integer id);

    Page<AnswerEntity> findByUserIdAndIsDeletedFalseAndQuestionVisibleTrue(String userId, Pageable pageable);

    List<AnswerEntity> findByUserIdAndIsDeletedFalseAndQuestionVisibleTrue(String userId);

    Page<AnswerEntity> findByIsDeletedFalseAndQuestionVisibleTrue(Pageable pageable);

    Page<AnswerEntity> findByQuestionIdAndQuestionVisibleTrue(Integer questionId, Pageable pageable);

    Page<AnswerEntity> findByUserId(String username, Pageable pageable);

    Page<AnswerEntity> findByIdInAndIsDeletedFalseAndQuestionVisibleTrue(@Valid Integer[] id, Pageable pageable);
}