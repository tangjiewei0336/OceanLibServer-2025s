package com.oriole.ocean.repository;

import com.oriole.ocean.common.po.mongo.QuestionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoQuestionRepository extends MongoRepository<QuestionEntity, String> {

    // 查询所有未删除且不是草稿的问题
    Page<QuestionEntity> findByIsDeletedFalseAndIsHiddenFalseAndIsPostedTrue(Pageable pageable);

    // 查询指定用户的所有未删除且不是草稿的问题
    Page<QuestionEntity> findByUserIdAndIsDeletedFalseAndIsHiddenFalseAndIsPostedTrue(String userId, Pageable pageable);

    QuestionEntity findByBindIdAndIsDeletedFalse(Integer id);

    long countByUserIdAndIsDeletedFalse(String userId);

    List<QuestionEntity> findByBindIdInAndIsDeletedFalseAndIsPostedTrue(List<Integer> questionIds);

    // 查询指定用户的所有未删除且不是草稿的问题
    Page<QuestionEntity> findByUserIdAndIsHiddenFalseAndIsPostedTrue(String username, Pageable pageable);


    Page<QuestionEntity> findByIsHiddenFalseAndIsPostedTrue(Pageable pageable);




}
