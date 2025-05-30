package com.oriole.ocean.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oriole.ocean.common.auth.AuthUser;
import com.oriole.ocean.common.dto.UserSearchDTO;
import com.oriole.ocean.common.enumerate.UserInfoLevel;
import com.oriole.ocean.common.po.mysql.UserEntity;
import com.oriole.ocean.common.vo.AuthUserEntity;
import com.oriole.ocean.common.vo.BusinessException;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.service.UserInfoServiceImpl;
import com.oriole.ocean.service.base.UserBaseInfoServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @RequestMapping(value = "/updateUserInfo",method = RequestMethod.PUT)
    public MsgEntity<UserEntity> updateUserInfo(@AuthUser AuthUserEntity authUser, @RequestBody UserEntity updatedInfo) {
        return new MsgEntity<>("SUCCESS", "1",
                userInfoService.updateUserInfo(authUser, updatedInfo));
    }

    @RequestMapping(value = "/searchUsers", method = RequestMethod.POST)
    public MsgEntity<Page<UserEntity>> searchUsers(
            @AuthUser AuthUserEntity authUser,
            @RequestBody UserSearchDTO searchParams) {
        // 调用服务层方法，传入权限级别
        Page<UserEntity> results = userInfoService.searchUsersWithPaging(
                authUser,
                searchParams.getUsername(),
                searchParams.getNickname(),
                searchParams.getRealname(),
                searchParams.getCollege(),
                searchParams.getMajor(),
                searchParams.getPageNum(),
                searchParams.getPageSize()
        );

        return new MsgEntity<>("SUCCESS", "1", results);
    }

    @RequestMapping(value = "/ban", method = RequestMethod.PUT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public MsgEntity<UserEntity> ban(@AuthUser AuthUserEntity authUser, @RequestParam String username) {
        UserEntity userEntity = userBaseInfoService.banUser(authUser, username);
        return new MsgEntity<>("SUCCESS", "1", userEntity);
    }

}
