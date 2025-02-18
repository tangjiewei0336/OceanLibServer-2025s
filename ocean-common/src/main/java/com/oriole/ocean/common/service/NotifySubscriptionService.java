package com.oriole.ocean.common.service;

import com.oriole.ocean.common.enumerate.NotifyAction;
import com.oriole.ocean.common.enumerate.NotifySubscriptionTargetType;
import com.oriole.ocean.common.po.mysql.NotifySubscriptionEntity;

import java.util.ArrayList;
import java.util.List;

public interface NotifySubscriptionService {
    void setNotifySubscription(String username, List<NotifyAction> notifyActionList, String targetID, NotifySubscriptionTargetType targetType);
}