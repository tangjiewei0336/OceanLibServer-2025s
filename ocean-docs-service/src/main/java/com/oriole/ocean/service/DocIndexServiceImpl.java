package com.oriole.ocean.service;

import com.oriole.ocean.common.po.mongo.IndexNodeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class DocIndexServiceImpl {

    @Autowired
    private MongoTemplate mongoTemplate;

    public IndexNodeEntity getAll(String indexString) {
        String[] indexs=indexString.split("\\-");
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(Integer.valueOf(indexs[0])));
        IndexNodeEntity root = mongoTemplate.findOne(query, IndexNodeEntity.class, "file_index");
        if (indexs.length == 1) {
            return root;
        } else {
            for (int i = 1; i < indexs.length; i++) {
                root = root.getNextNodes().get(Integer.valueOf(indexs[i]));
            }
            return root;
        }
    }
}
