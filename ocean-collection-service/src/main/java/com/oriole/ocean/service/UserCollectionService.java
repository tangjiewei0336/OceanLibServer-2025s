package com.oriole.ocean.service;

import com.alibaba.fastjson.JSONObject;
import com.oriole.ocean.common.enumerate.MainType;
import com.oriole.ocean.common.po.mysql.UserCollectionEntity;

public interface UserCollectionService {
    void collectionStatisticsChange(Integer itemID, String addNumber, MainType mainType);
    
    UserCollectionEntity getCollectionByUsername(String username, MainType mainType);
    
    UserCollectionEntity.CollectionEntity getCollectionByUsernameAndCollectionID(String username, String collectionID, MainType mainType);
    
    UserCollectionEntity addCollection(String collectionID, String username, String newName, Boolean isPublic, String desc, MainType mainType);
    
    UserCollectionEntity changeCollectionItem(JSONObject changeList, String username, Integer itemID, MainType mainType);
    
    UserCollectionEntity changeCollection(String username, String collectionID, String newName, Boolean isPublic, String desc, MainType mainType);
    
    Integer deleteOneCollectionItem(String username, String collectionID, Integer itemID, MainType mainType);
    
    UserCollectionEntity.CollectionEntity deleteOneCollection(String username, String collectionID, MainType mainType);
    
    void setOneCollection(UserCollectionEntity userCollectionEntity, MainType mainType);
} 