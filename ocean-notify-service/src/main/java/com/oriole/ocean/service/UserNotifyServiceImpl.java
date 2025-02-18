package com.oriole.ocean.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oriole.ocean.dao.UserNotifyDao;
import com.oriole.ocean.common.po.mysql.UserNotifyEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class UserNotifyServiceImpl extends ServiceImpl<UserNotifyDao, UserNotifyEntity> {
    @Resource
    private UserNotifyDao userNotifyDao;

    //用户消息表
    public List<UserNotifyEntity> getAllNotifyByUsernameAndLastPullDate(String username, Date latestPullDate){
        return userNotifyDao.getAllNotifyByUsernameAndLastPullDate(username,latestPullDate);
    }

    // 查询用户消息表中最后的更新时间
    public Date getUserNotifyLastUpdateTime() {
        QueryWrapper<UserNotifyEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("max(build_date) as buildDate");
        UserNotifyEntity userNotifyEntity = getOne(queryWrapper);
        if (userNotifyEntity == null) {
            return null;
        } else {
            return userNotifyEntity.getBuildDate();
        }
    }

    // 批量插入消息至用户消息列表
    public void setUserNotifyList(List<UserNotifyEntity> userNotifyEntities) {
        saveBatch(userNotifyEntities);
    }
}
