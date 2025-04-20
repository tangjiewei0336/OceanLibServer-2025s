package com.oriole.ocean.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.oriole.ocean.common.enumerate.UserInfoLevel;
import com.oriole.ocean.common.po.mysql.UserEntity;
import com.oriole.ocean.common.po.mysql.UserExtraEntity;
import com.oriole.ocean.common.vo.AuthUserEntity;
import com.oriole.ocean.common.vo.BusinessException;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.dao.UserDao;
import com.oriole.ocean.dao.UserExtraDao;
import com.oriole.ocean.service.base.UserBaseInfoServiceImpl;
import com.oriole.ocean.service.base.UserCertificationServiceImpl;
import com.oriole.ocean.service.base.UserExtraInfoServiceImpl;
import com.oriole.ocean.service.base.UserWalletServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserInfoServiceImpl {
    @Autowired
    UserBaseInfoServiceImpl userService;
    @Autowired
    UserExtraInfoServiceImpl userExtraService;
    @Autowired
    UserCertificationServiceImpl userCertificationService;
    @Autowired
    UserWalletServiceImpl walletService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private UserExtraDao userExtraDao;

    public UserEntity getUserInfo(String username, UserInfoLevel userInfoLevel) {
        UserEntity userEntity = userService.getById(username);
        userEntity.setPassword(null);

        //加载认证，如果有
        Integer certID = userEntity.getCertID();
        if(certID!=null) {
            userEntity.setUserCertificationEntity(userCertificationService.getById(certID));
        }

        switch (userInfoLevel){
            case LIMITED:
                userEntity.setEmail(null);
                userEntity.setPhoneNum(null);
                userEntity.setStudentID(null);
                userEntity.setUserExtraEntity(userExtraService.getById(username));
                return userEntity;
            case ALL:
                userEntity.setWallet(walletService.getById(username));
                userEntity.setUserExtraEntity(userExtraService.getById(username));
                return userEntity;
            case BASE:
                userEntity.setWallet(walletService.getById(username));
                return userEntity;
            default:
                throw new RuntimeException("not support this userInfo level");
        }
    }

    public UserEntity updateUserInfo(AuthUserEntity authUser, UserEntity updatedInfo) {
        UserEntity userEntity = userService.getById(authUser.getUsername());
        if (userEntity == null) {
            throw new BusinessException("-1", "用户不存在");
        }
        if(authUser.isSuperAdmin()) {
            if (updatedInfo.getRole() != null) { // 允许更新角色
                userEntity.setRole(updatedInfo.getRole());
            }

        }
        if(authUser.isAdmin()) {
            if (updatedInfo.getNickname() != null) {
                userEntity.setNickname(updatedInfo.getNickname());
            }
            if (updatedInfo.getPhoneNum() != null) {
                userEntity.setPhoneNum(updatedInfo.getPhoneNum());
            }
            if (updatedInfo.getStudentID() != null) {
                userEntity.setStudentID(updatedInfo.getStudentID());
            }
            if (updatedInfo.getRealname() != null) {
                userEntity.setRealname(updatedInfo.getRealname());
            }
            if (updatedInfo.getAvatar() != null) {
                userEntity.setAvatar(updatedInfo.getAvatar());
            }
            if (updatedInfo.getEmail() != null) {
                userEntity.setEmail(updatedInfo.getEmail());
            }

            UserExtraEntity updateInfoExtraEntity = updatedInfo.getUserExtraEntity();
            UserExtraEntity tempUserExtraEntity = userEntity.getUserExtraEntity();

            if (updateInfoExtraEntity.getCollege() != null) {
                tempUserExtraEntity.setCollege(updateInfoExtraEntity.getCollege());
            }
            if (updateInfoExtraEntity.getMajor() != null) {
                tempUserExtraEntity.setMajor(updateInfoExtraEntity.getMajor());
            }
            if (updateInfoExtraEntity.getBirthday() != null) {
                tempUserExtraEntity.setBirthday(updateInfoExtraEntity.getBirthday());
            }
            if (updateInfoExtraEntity.getSex() != null) {
                tempUserExtraEntity.setSex(updateInfoExtraEntity.getSex());
            }
            if (updateInfoExtraEntity.getPersonalSignature() != null) {
                tempUserExtraEntity.setPersonalSignature(updateInfoExtraEntity.getPersonalSignature());
            }
        }else {
            throw new BusinessException("-2", "无权限更新此信息");  // 一般也不触发
        }

        // 更新用户信息到数据库
        userService.updateById(userEntity);
        return userEntity;
    }


    public List<UserEntity> searchUsers(AuthUserEntity authUser, UserInfoLevel infoLevel,
                                        String username, String nickname, String realname,
                                        String college, String major) {
        // 检查搜索权限
        if (!authUser.isAdmin() && !authUser.isSuperAdmin()) {
            // 如果不是管理员，则限制信息访问级别
            infoLevel = UserInfoLevel.LIMITED;
        }

        // 创建用户表查询条件
        QueryWrapper<UserEntity> userQuery = new QueryWrapper<>();
        if (username != null) userQuery.like("username", username);
        if (nickname != null) userQuery.like("nickname", nickname);
        if (realname != null) userQuery.like("realname", realname);

        List<UserEntity> users = userDao.selectList(userQuery); // 使用 userDao 代替 userMapper

        // 如果需要查询额外信息表
        if (college != null || major != null) {
            QueryWrapper<UserExtraEntity> extraQuery = new QueryWrapper<>();
            if (college != null) extraQuery.like("college", college);
            if (major != null) extraQuery.like("major", major);

            List<UserExtraEntity> extras = userExtraDao.selectList(extraQuery); // 使用 userExtraDao 代替 userExtraMapper
            Set<String> matchingUsernames = extras.stream()
                    .map(UserExtraEntity::getUsername)
                    .collect(Collectors.toSet());

            // 过滤出同时匹配两个条件的用户
            users = users.stream()
                    .filter(user -> matchingUsernames.contains(user.getUsername()))
                    .collect(Collectors.toList());

            // 为每个用户加载额外信息
            for (UserEntity user : users) {
                UserExtraEntity extra = extras.stream()
                        .filter(e -> e.getUsername().equals(user.getUsername()))
                        .findFirst().orElse(null);
                user.setUserExtraEntity(extra);
            }
        }

        // 根据权限级别过滤用户信息 - 使用现有的 getUserInfo 方法
        List<UserEntity> filteredResults = new ArrayList<>();
        for (UserEntity user : users) {
            // 不直接使用 user，而是通过 getUserInfo 方法获取过滤后的用户信息
            UserEntity filteredUser = getUserInfo(user.getUsername(), infoLevel);
            filteredResults.add(filteredUser);
        }

        return filteredResults;
    }
}
