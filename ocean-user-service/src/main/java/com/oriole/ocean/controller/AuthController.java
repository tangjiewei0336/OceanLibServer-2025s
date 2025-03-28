package com.oriole.ocean.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.oriole.ocean.common.po.mysql.UserEntity;
import com.oriole.ocean.service.base.UserBaseInfoServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.oriole.ocean.common.vo.MsgEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/userAuth")
public class AuthController {

    @Autowired
    UserBaseInfoServiceImpl userBaseInfoService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public MsgEntity<UserEntity> login(@RequestParam String username, @RequestParam String password) {
        UserEntity userEntity = userBaseInfoService.getUserInfoByUsernameAndPassword(username, password);
        return new MsgEntity<>("SUCCESS","1", userEntity);
    }

}
