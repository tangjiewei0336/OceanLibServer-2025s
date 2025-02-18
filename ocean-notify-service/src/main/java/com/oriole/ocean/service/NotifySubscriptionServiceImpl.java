package com.oriole.ocean.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oriole.ocean.common.enumerate.NotifyAction;
import com.oriole.ocean.common.enumerate.NotifySubscriptionTargetType;
import com.oriole.ocean.common.service.NotifySubscriptionService;
import com.oriole.ocean.dao.NotifySubscriptionDao;
import com.oriole.ocean.common.po.mysql.NotifySubscriptionEntity;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@DubboService
@Transactional
public class NotifySubscriptionServiceImpl extends ServiceImpl<NotifySubscriptionDao, NotifySubscriptionEntity> implements NotifySubscriptionService {
    // 查询指定目标行为的全部订阅用户
    public List<NotifySubscriptionEntity> getAllSubscriptionByTargetAndAction(String targetID, NotifySubscriptionTargetType targetType, NotifyAction action) {
        QueryWrapper<NotifySubscriptionEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("target_id", targetID);
        queryWrapper.eq("target_type", targetType);
        queryWrapper.eq("action", action);
        return list(queryWrapper);
    }

    // 插入用户订阅
    public void setNotifySubscription(String username,List<NotifyAction> notifyActionList,String targetID, NotifySubscriptionTargetType targetType) {
        List<NotifySubscriptionEntity> notifySubscriptionEntities = new ArrayList<>();
        for (NotifyAction action: notifyActionList) {
            notifySubscriptionEntities.add(new NotifySubscriptionEntity(targetID, targetType, action, username));
        }
        saveBatch(notifySubscriptionEntities);
    }
}
