package com.oriole.ocean.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.oriole.ocean.common.auth.AuthUser;
import com.oriole.ocean.common.enumerate.*;
import com.oriole.ocean.common.enumerate.MainType;
import com.oriole.ocean.common.po.mongo.UserBehaviorEntity;
import com.oriole.ocean.common.po.mysql.UserCollectionEntity;
import com.oriole.ocean.common.service.UserBehaviorService;
import com.oriole.ocean.common.vo.AuthUserEntity;
import com.oriole.ocean.common.vo.BusinessException;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.service.UserCollectionServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.oriole.ocean.common.enumerate.BehaviorExtraInfo.IN_COLLECTION;
import static com.oriole.ocean.common.enumerate.ResultCode.SUCCESS;

@RestController
@Slf4j
@RequestMapping("/collectionService")
public class UserCollectionController {

    @Autowired
    UserCollectionServiceImpl userCollectionService;

    @DubboReference
    UserBehaviorService userBehaviorService;

    @RequestMapping(value = "/getCollection", method = RequestMethod.GET)
    public MsgEntity<UserCollectionEntity> getCollection(@AuthUser AuthUserEntity authUser,
                                                         @RequestParam(required = false) String username,
                                                         @RequestParam MainType mainType) {
        username = authUser.getAllowOperationUsername(username);

        UserCollectionEntity userCollectionEntity = userCollectionService.getCollectionByUsername(username, mainType);
        return new MsgEntity<>("SUCCESS", "1", userCollectionEntity);
    }

    @RequestMapping(value = "/addCollection", method = RequestMethod.POST)
    public MsgEntity<UserCollectionEntity> addCollection(@AuthUser AuthUserEntity authUser,
                                                         @RequestParam(required = false) String username,
                                                         @RequestParam String newName,
                                                         @RequestParam Boolean isPublic, @RequestParam String desc,
                                                         @RequestParam MainType mainType) {
        username = authUser.getAllowOperationUsername(username);

        String collectionID = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
        UserCollectionEntity userCollectionEntity = userCollectionService.addCollection(collectionID, username, newName, isPublic, desc, mainType);
        return new MsgEntity<>("SUCCESS", "1", userCollectionEntity);
    }

    @RequestMapping(value = "/deleteCollection", method = RequestMethod.GET)
    public MsgEntity<String> deleteCollection(@AuthUser AuthUserEntity authUser,
                                              @RequestParam(required = false) String username,
                                              @RequestParam String collectionID,
                                              @RequestParam MainType mainType) {
        username = authUser.getAllowOperationUsername(username);

        UserCollectionEntity.CollectionEntity collectionEntity = userCollectionService.deleteOneCollection(username, collectionID, mainType);
        if (collectionEntity == null) {
            throw new BusinessException("-2", "未找到指定的收藏夹");
        }
        // 进行到了此处说明变更肯定发生了
        // 需要一并清空用户的收藏情况记录
        for (Integer itemID : collectionEntity.getItems()) { // 遍历被删除的收藏夹中的文件对应用户行为记录
            UserBehaviorEntity userBehaviorEntityQuery = new UserBehaviorEntity(itemID, mainType, username, BehaviorType.DO_COLLECTION);
            JSONArray collectionList = (JSONArray) userBehaviorService.findBehaviorRecord(userBehaviorEntityQuery).getExtraInfo(IN_COLLECTION);

            collectionList.remove(collectionID);
            if (collectionList.isEmpty()) {//所有的收藏已经被全部删除了
                // 移除收藏量统计
                userCollectionService.collectionStatisticsChange(itemID, "-1", mainType);
                // 移除用户行为记录
                userBehaviorService.deleteBehaviorRecord(userBehaviorEntityQuery);
            } else { // 还有收藏记录存在，更新记录
                userBehaviorService.updateBehaviorRecordExtraInfo(userBehaviorEntityQuery, IN_COLLECTION, collectionList);
            }
        }
        return new MsgEntity<>(SUCCESS);
    }

    @RequestMapping(value = "/changeCollectionInfo", method = RequestMethod.POST)
    public MsgEntity<UserCollectionEntity> changeCollectionInfo(@AuthUser AuthUserEntity authUser,
                                                                @RequestParam(required = false) String username,
                                                                @RequestParam String collectionID, @RequestParam String newName,
                                                                @RequestParam Boolean isPublic, @RequestParam String desc,
                                                                @RequestParam MainType mainType) {
        username = authUser.getAllowOperationUsername(username);

        UserCollectionEntity userCollectionEntity = userCollectionService.changeCollection(username, collectionID, newName, isPublic, desc, mainType);
        return new MsgEntity<>("SUCCESS", "1", userCollectionEntity);
    }

    @RequestMapping(value = "/changeCollectionItem", method = RequestMethod.POST)
    public MsgEntity<UserCollectionEntity> changeCollectionItem(@AuthUser AuthUserEntity authUser,
                                                                @RequestParam(required = false) String username,
                                                                @RequestParam String changedList, @RequestParam Integer itemID,
                                                                @RequestParam MainType mainType) {
        username = authUser.getAllowOperationUsername(username);

        JSONObject changedCollectionList = JSON.parseObject(changedList);
        UserCollectionEntity userCollectionEntity = userCollectionService.changeCollectionItem(changedCollectionList, username, itemID, mainType);
        if (userCollectionEntity == null) {
            throw new BusinessException("-2", "收藏夹内容未发生任何变更");
        }
        // 进行到了此处说明变更肯定发生了
        UserBehaviorEntity userBehaviorEntity = new UserBehaviorEntity(itemID, mainType, username, BehaviorType.DO_COLLECTION);
        boolean existBehavior = userBehaviorService.findBehaviorRecord(userBehaviorEntity) != null;
        if (!existBehavior) {
            // 用户是首次收藏，没有此前的用户行为记录，直接增加收藏量
            userCollectionService.collectionStatisticsChange(itemID, "+1", mainType);
        }
        JSONArray existInCollectionList = new JSONArray();// 找到存在于那些收藏夹
        for (String collectionID : changedCollectionList.keySet()) {
            if (changedCollectionList.getBoolean(collectionID)) {
                existInCollectionList.add(collectionID);
            }
        }

        if (existInCollectionList.isEmpty()) { // 用户彻底移除了关于某个文件的全部收藏
            // 移除文件收藏量统计
            userCollectionService.collectionStatisticsChange(itemID, "-1", mainType);
            // 移除用户行为记录
            userBehaviorService.deleteBehaviorRecord(userBehaviorEntity);
        } else { // 只要不是全部删除了，我们就记录用户行为
            if(!existBehavior){
                userBehaviorEntity.setExtraInfo(IN_COLLECTION, existInCollectionList);
                userBehaviorService.setBehaviorRecord(userBehaviorEntity);
            }else {
                userBehaviorService.updateBehaviorRecordExtraInfo(userBehaviorEntity, IN_COLLECTION, existInCollectionList);
            }
        }
        return new MsgEntity<>("SUCCESS", "1", userCollectionEntity);
    }

    @RequestMapping(value = "/deleteCollectionItem", method = RequestMethod.GET)
    public MsgEntity<String> deleteCollectedItem(@AuthUser AuthUserEntity authUser,
                                                 @RequestParam(required = false) String username,
                                                 @RequestParam String collectionID,
                                                 @RequestParam Integer itemID,
                                                 @RequestParam MainType mainType) {
        username = authUser.getAllowOperationUsername(username);

        Integer removedItemID = userCollectionService.deleteOneCollectionItem(username, collectionID, itemID, mainType);
        if (removedItemID == null) {
            throw new BusinessException("-2", "未找到指定的收藏夹或没有变更发生");
        }
        // 进行到了此处说明肯定有删除发生
        UserBehaviorEntity userBehaviorEntityQuery = new UserBehaviorEntity(itemID, mainType, username, BehaviorType.DO_COLLECTION);
        JSONArray collectionList = (JSONArray) userBehaviorService.findBehaviorRecord(userBehaviorEntityQuery).getExtraInfo(IN_COLLECTION);
        collectionList.remove(collectionID);

        if (collectionList.isEmpty()) {//所有的收藏已经被全部删除了
            // 移除文件收藏量统计
            userCollectionService.collectionStatisticsChange(itemID, "-1", mainType);
            // 移除用户行为记录
            userBehaviorService.deleteBehaviorRecord(userBehaviorEntityQuery);
        } else { // 还有收藏记录存在，更新记录
            userBehaviorService.updateBehaviorRecordExtraInfo(userBehaviorEntityQuery, IN_COLLECTION, collectionList);
        }
        return new MsgEntity<>(SUCCESS);
    }

    @RequestMapping(value = "/getCollectionItemList", method = RequestMethod.GET)
    public MsgEntity<List<Integer>> getCollectionItemList(@AuthUser AuthUserEntity authUser,
                                                             @RequestParam(required = false) String username,
                                                             @RequestParam String collectionID,
                                                             @RequestParam MainType mainType) {
        username = authUser.getAllowOperationUsername(username);

        UserCollectionEntity.CollectionEntity collectionEntity = userCollectionService.getCollectionByUsernameAndCollectionID(username, collectionID, mainType);
        return new MsgEntity<>("SUCCESS", "1", collectionEntity.getItems());
    }
}
