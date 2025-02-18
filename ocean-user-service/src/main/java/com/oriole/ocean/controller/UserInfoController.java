package com.oriole.ocean.controller;

import com.oriole.ocean.common.auth.AuthUser;
import com.oriole.ocean.common.enumerate.UserInfoLevel;
import com.oriole.ocean.common.po.mysql.UserEntity;
import com.oriole.ocean.common.vo.AuthUserEntity;
import com.oriole.ocean.common.vo.BusinessException;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.service.UserInfoServiceImpl;
import com.oriole.ocean.service.base.UserBaseInfoServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/userInfoService")
public class UserInfoController {

    @Autowired
    UserBaseInfoServiceImpl userBaseInfoService;
    @Autowired
    UserInfoServiceImpl userInfoService;

    @RequestMapping(value = "/checkSameUsername",method = RequestMethod.GET)
    public MsgEntity<String> checkSameUsername(@RequestParam String username) {
        UserEntity userEntity = userBaseInfoService.getById(username);
        if (userEntity == null) {
            return new MsgEntity<>("SUCCESS", "1", "没有检测到重复用户名");
        } else {
            throw new BusinessException("-2", "用户名不得重复");
        }
    }
    
    @RequestMapping(value = "/getUserLimitedInfo",method = RequestMethod.GET)
    public MsgEntity<UserEntity> getUserLimitedInfo(@RequestParam String username) {
        return new MsgEntity<>("SUCCESS", "1", userInfoService.getUserInfo(username, UserInfoLevel.LIMITED));
    }

    @RequestMapping(value = "/getUserBaseInfo",method = RequestMethod.GET)
    public MsgEntity<UserEntity> getUserBaseInfo(@AuthUser AuthUserEntity authUser) {
        return new MsgEntity<>("SUCCESS", "1",
                userInfoService.getUserInfo(authUser.getUsername(), UserInfoLevel.BASE));
    }

    @RequestMapping(value = "/getUserAllInfo",method = RequestMethod.GET)
    public MsgEntity<UserEntity> getUserAllInfo(@AuthUser AuthUserEntity authUser) {
        return new MsgEntity<>("SUCCESS", "1",
                userInfoService.getUserInfo(authUser.getUsername(), UserInfoLevel.ALL));
    }
}
