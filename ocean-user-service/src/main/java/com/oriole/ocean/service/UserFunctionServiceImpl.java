package com.oriole.ocean.service;

import com.oriole.ocean.common.enumerate.BehaviorType;
import com.oriole.ocean.common.po.mongo.UserBehaviorEntity;
import com.oriole.ocean.common.service.UserBehaviorService;
import com.oriole.ocean.common.vo.BusinessException;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class UserFunctionServiceImpl {

    @DubboReference
    UserBehaviorService userBehaviorService;

    public void doDailyCheckIn(String username) throws Exception {
        // 检查用户是否有过签到
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String start = sdf.format(new Date());

        UserBehaviorEntity userBehaviorEntity = new UserBehaviorEntity(username, BehaviorType.DO_DAILY_CHECK);
        List<UserBehaviorEntity> userBehaviorEntityList = userBehaviorService.findAllBehaviorRecords(userBehaviorEntity, sdf.parse(start), new Date());
        if (!userBehaviorEntityList.isEmpty()) {
            throw new BusinessException("-2","请勿重复签到");
        }
        userBehaviorService.setBehaviorRecord(userBehaviorEntity);
    }

    @RequestMapping(value = "getDailyCheckIn",method = RequestMethod.GET)
    public List<UserBehaviorEntity> getDailyCheckIn(String username, Integer year, Integer month) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = sdf.parse(year + "-" + month + "-1");
        Date endDate = sdf.parse(year + "-" + (month + 1) + "-1");
        UserBehaviorEntity userBehaviorEntity = new UserBehaviorEntity(username, BehaviorType.DO_DAILY_CHECK);
        return userBehaviorService.findAllBehaviorRecords(userBehaviorEntity, startDate, endDate);
    }
}
