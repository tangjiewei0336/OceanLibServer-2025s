package com.oriole.ocean.common.service;

import com.oriole.ocean.common.enumerate.UserInfoLevel;
import com.oriole.ocean.common.po.mysql.UserEntity;
import com.oriole.ocean.common.vo.AuthUserEntity;

public interface UserInfoService {
    UserEntity getUserInfo(String username, UserInfoLevel userInfoLevel);
    UserEntity updateUserInfo(AuthUserEntity authUser, UserEntity updatedInfo);
} 