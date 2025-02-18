package com.oriole.ocean.controller;

import com.oriole.ocean.common.auth.AuthUser;
import com.oriole.ocean.common.po.mysql.UserNotifyEntity;
import com.oriole.ocean.common.vo.AuthUserEntity;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.service.NotifyServiceImpl;
import com.oriole.ocean.service.NotifySubscriptionServiceImpl;
import com.oriole.ocean.service.UserNotifyServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/notify")
public class UserNotifyController {

    @Autowired
    NotifyServiceImpl notifyService;

    @Autowired
    NotifySubscriptionServiceImpl notifySubscriptionService;

    @Autowired
    UserNotifyServiceImpl userNotifyService;

    @RequestMapping("/getLatestNotifications")
    public MsgEntity<List<UserNotifyEntity>> getLatestNotifications(
            @AuthUser AuthUserEntity authUser,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Date lastPullDate) {
        username = authUser.getAllowOperationUsername(username);

        return new MsgEntity<>("SUCCESS", "1", userNotifyService.getAllNotifyByUsernameAndLastPullDate(username, lastPullDate));
    }
}
