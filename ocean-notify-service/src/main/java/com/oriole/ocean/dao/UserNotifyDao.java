package com.oriole.ocean.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oriole.ocean.common.po.mysql.UserNotifyEntity;

import java.util.Date;
import java.util.List;

public interface UserNotifyDao extends BaseMapper<UserNotifyEntity> {
    List<UserNotifyEntity> getAllNotifyByUsernameAndLastPullDate(String username, Date latestPullDate);
}
