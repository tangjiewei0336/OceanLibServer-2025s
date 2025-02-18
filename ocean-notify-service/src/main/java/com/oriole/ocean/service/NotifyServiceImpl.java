package com.oriole.ocean.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oriole.ocean.common.enumerate.NotifyAction;
import com.oriole.ocean.common.enumerate.NotifyType;
import com.oriole.ocean.common.po.mongo.UserBehaviorEntity;
import com.oriole.ocean.common.service.NotifyService;
import com.oriole.ocean.dao.NotifyDao;
import com.oriole.ocean.common.po.mysql.NotifyEntity;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@DubboService
public class NotifyServiceImpl extends ServiceImpl<NotifyDao, NotifyEntity> implements NotifyService {
    public void addNotify(NotifyEntity notifyEntity){
        save(notifyEntity);
    }

    // 根据用户行为生产消息
    public void addNotifyByBehaviorRecord(UserBehaviorEntity userBehaviorEntity) {
        // 收藏、每日签到、阅读等行为都不需要记录为通知行为
        NotifyEntity notifyEntity = new NotifyEntity(NotifyType.REMIND,userBehaviorEntity.getDoUsername());
        switch (userBehaviorEntity.getBehaviorType()) {
            case DO_DOWNLOAD:
                notifyEntity.setAction(NotifyAction.DOWNLOAD);
                break;
            case DO_LIKE:
                notifyEntity.setAction(NotifyAction.LIKE);
                break;
            case DO_COMMENT_LIKE:
                notifyEntity.setAction(NotifyAction.LIKE_COMMENT);
                notifyEntity.setCommentID(userBehaviorEntity.getExtraInfo().getString("commentID"));
                break;
            default:
                return;
        }
        notifyEntity.setUserBehaviorID(userBehaviorEntity.getId());
        notifyEntity.setTargetIDAndType(userBehaviorEntity.getBindID(),userBehaviorEntity.getType());
        addNotify(notifyEntity);
    }

    // 查询指定时间之后产生的所有消息
    public List<NotifyEntity> getAllNotifyAfterTime(Date date) {
        QueryWrapper<NotifyEntity> queryWrapper = new QueryWrapper<>();
        if (date != null) {
            queryWrapper.gt("build_date", date);
        }
        return list(queryWrapper);
    }

}
