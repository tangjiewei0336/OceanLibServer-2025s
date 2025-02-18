package com.oriole.ocean.controller;

import com.oriole.ocean.common.auth.AuthUser;
import com.oriole.ocean.common.po.mongo.UserBehaviorEntity;
import com.oriole.ocean.common.vo.AuthUserEntity;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.service.UserFunctionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/userFunctionService")
public class UserFunctionController {

    @Autowired
    UserFunctionServiceImpl userFunctionService;

    @RequestMapping(value = "doDailyCheckIn",method = RequestMethod.GET)
    public MsgEntity<String> doDailyCheckIn(@AuthUser AuthUserEntity authUser) throws Exception {
        userFunctionService.doDailyCheckIn(authUser.getUsername());
        return new MsgEntity<>("SUCCESS", "1", "签到成功");
    }

    @RequestMapping(value = "getDailyCheckIn",method = RequestMethod.GET)
    public MsgEntity<List<UserBehaviorEntity>> getDailyCheckIn(@AuthUser AuthUserEntity authUser, @RequestParam Integer year, @RequestParam Integer month) throws Exception {
        return new MsgEntity<>("SUCCESS", "1", userFunctionService.getDailyCheckIn(authUser.getUsername(),year,month));
    }
}
