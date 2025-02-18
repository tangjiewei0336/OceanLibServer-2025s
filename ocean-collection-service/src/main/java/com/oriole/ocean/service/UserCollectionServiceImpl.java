package com.oriole.ocean.service;

import com.alibaba.fastjson.JSONObject;
import com.oriole.ocean.common.enumerate.DocStatisticItemType;
import com.oriole.ocean.common.enumerate.MainType;
import com.oriole.ocean.common.po.mysql.UserCollectionEntity;
import com.oriole.ocean.common.service.FileExtraService;
import com.oriole.ocean.common.vo.BusinessException;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserCollectionServiceImpl {

    @Autowired
    private MongoTemplate mongoTemplate;

    @DubboReference
    private FileExtraService fileExtraService;

    public void collectionStatisticsChange(Integer itemID, String addNumber, MainType mainType) {
        switch (mainType){
            case DOCUMENT:
                fileExtraService.docBaseStatisticsInfoChange(itemID, addNumber, DocStatisticItemType.COLLECTION);
                break;
            case NOTE:
                break;
        }
    }

    private String getCollectionName(MainType mainType){
        return "user_collection_" + mainType.toString();
    }

    public UserCollectionEntity getCollectionByUsername(String username, MainType mainType) {
        Query query = new Query();
        query.addCriteria(Criteria.where("username").is(username));
        UserCollectionEntity userCollectionEntity = mongoTemplate.findOne(query, UserCollectionEntity.class, getCollectionName(mainType));
        if (userCollectionEntity == null) {
            userCollectionEntity = new UserCollectionEntity(username, new ArrayList<>());
            setOneCollection(userCollectionEntity, mainType);
        }
        return userCollectionEntity;
    }

    public UserCollectionEntity.CollectionEntity getCollectionByUsernameAndCollectionID(String username, String collectionID, MainType mainType) {
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(Criteria.where("username").is(username)));
        operations.add(Aggregation.unwind("collection"));
        operations.add(Aggregation.match(Criteria.where("collection.collectionID").is(collectionID)));
        operations.add(Aggregation.project()
                .andExpression("collection.name").as("name")
                .andExpression("collection.isPublic").as("isPublic")
                .andExpression("collection.items").as("items"));

        Aggregation aggregation = Aggregation.newAggregation(operations);

        AggregationResults<UserCollectionEntity.CollectionEntity> results = mongoTemplate.aggregate(aggregation,
                getCollectionName(mainType), UserCollectionEntity.CollectionEntity.class);
        return results.getMappedResults().get(0);
    }

    public UserCollectionEntity addCollection(String collectionID, String username, String newName, Boolean isPublic, String desc, MainType mainType) {
        Query query = new Query();
        query.addCriteria(Criteria.where("username").is(username));
        Update update = new Update();

        UserCollectionEntity userCollectionEntity = getCollectionByUsername(username, mainType);
        List<UserCollectionEntity.CollectionEntity> collectionList = userCollectionEntity.getCollection();
        for (UserCollectionEntity.CollectionEntity collection : collectionList) {
            if (collection.getName().equals(newName)) {
                throw new BusinessException("-2", "Can't add a same collection");
            }
        }

        UserCollectionEntity.CollectionEntity collectionEntity = userCollectionEntity.new CollectionEntity();
        collectionEntity.setCollectionID(collectionID);
        collectionEntity.setName(newName);
        collectionEntity.setIsPublic(isPublic);
        if (!desc.isEmpty()) {
            collectionEntity.setDesc(desc);
        }
        collectionEntity.setItems(new ArrayList<>());
        userCollectionEntity.getCollection().add(collectionEntity);

        update.set("collection", userCollectionEntity.getCollection());

        mongoTemplate.upsert(query, update, UserCollectionEntity.class, getCollectionName(mainType));
        return userCollectionEntity;
    }

    public UserCollectionEntity changeCollectionItem(JSONObject changeList, String username, Integer itemID, MainType mainType) {
        Query query = new Query();
        query.addCriteria(Criteria.where("username").is(username));
        Update update = new Update();

        // 检查是否有变动
        boolean isChanged = false;

        UserCollectionEntity userCollectionEntity = getCollectionByUsername(username, mainType);
        List<UserCollectionEntity.CollectionEntity> collectionList = userCollectionEntity.getCollection();

        for (UserCollectionEntity.CollectionEntity collection : collectionList) {
            Boolean existStatus = changeList.getBoolean(collection.getCollectionID());//对应收藏夹的名字是否出现在变更后的列表中

            int index = collection.getItems().indexOf(itemID);

            if (existStatus == null || !existStatus) {//变更后的列表不存在这个收藏夹
                if (index != -1) { //如果变更前存在就删除
                    isChanged = true;
                    collection.getItems().remove(index);
                }
            } else { //变更后的列表存在这个收藏夹
                if (index == -1) {  //如果变更前不存在就新增
                    isChanged = true;
                    collection.getItems().add(itemID);
                }
            }
        }
        // 如果没有任何更改，返回空，否则返回当前用户的收藏夹列表
        if (!isChanged) {
            return null;
        }
        update.set("collection", collectionList);

        mongoTemplate.upsert(query, update, UserCollectionEntity.class, getCollectionName(mainType));
        return userCollectionEntity;
    }

    public UserCollectionEntity changeCollection(String username, String collectionID, String newName, Boolean isPublic, String desc, MainType mainType) {

        Query query = new Query();
        query.addCriteria(Criteria.where("username").is(username));
        Update update = new Update();

        UserCollectionEntity userCollectionEntity = getCollectionByUsername(username, mainType);

        for (UserCollectionEntity.CollectionEntity collectionEntity : userCollectionEntity.getCollection()) {
            if (collectionEntity.getCollectionID().equals(collectionID)) {
                collectionEntity.setName(newName);
                if (!collectionEntity.getIsPublic()) {
                    collectionEntity.setIsPublic(isPublic);
                }
                collectionEntity.setDesc(desc);
            }
        }
        update.set("collection", userCollectionEntity.getCollection());

        mongoTemplate.upsert(query, update, UserCollectionEntity.class, getCollectionName(mainType));
        return userCollectionEntity;
    }

    public Integer deleteOneCollectionItem(String username, String collectionID, Integer itemID, MainType mainType) {
        Query query = new Query();
        query.addCriteria(Criteria.where("username").is(username));
        Update update = new Update();

        // 检查是否有变动
        Integer removedItemID = null;

        UserCollectionEntity userCollectionEntity = getCollectionByUsername(username, mainType);

        for (UserCollectionEntity.CollectionEntity collectionEntity : userCollectionEntity.getCollection()) {
            if (collectionEntity.getCollectionID().equals(collectionID)) {
                collectionEntity.getItems().remove(itemID);
                removedItemID = itemID;
                break;
            }
        }

        update.set("collection", userCollectionEntity.getCollection());
        mongoTemplate.upsert(query, update, UserCollectionEntity.class, getCollectionName(mainType));
        return removedItemID;
    }

    public UserCollectionEntity.CollectionEntity deleteOneCollection(String username, String collectionID, MainType mainType) {
        Query query = new Query();
        query.addCriteria(Criteria.where("username").is(username));
        Update update = new Update();

        // 检查是否有变动
        UserCollectionEntity.CollectionEntity removedCollectionEntity = null;

        UserCollectionEntity userCollectionEntity = getCollectionByUsername(username, mainType);

        for (UserCollectionEntity.CollectionEntity collectionEntity : userCollectionEntity.getCollection()) {
            if (collectionEntity.getCollectionID().equals(collectionID)) {
                userCollectionEntity.getCollection().remove(collectionEntity);
                removedCollectionEntity = collectionEntity;
                break;
            }
        }

        update.set("collection", userCollectionEntity.getCollection());
        mongoTemplate.upsert(query, update, UserCollectionEntity.class, getCollectionName(mainType));
        return removedCollectionEntity;
    }

    public void setOneCollection(UserCollectionEntity userCollectionEntity, MainType mainType) {
        mongoTemplate.insert(userCollectionEntity, getCollectionName(mainType));
    }
}
