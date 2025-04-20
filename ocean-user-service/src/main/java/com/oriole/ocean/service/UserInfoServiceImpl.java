package com.oriole.ocean.service;

import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

import java.util.*;
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

    /**
     * 未分页搜索用户信息
     * @return 用户信息的 List
     */
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

        List<UserEntity> users = userDao.selectList(userQuery);

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

    /**
     * 分页搜索用户信息，包含用户基本信息和额外信息
     * @return 包含分页信息和用户列表的 Page 对象
     */
    public Page<UserEntity> searchUsersWithPaging(
            AuthUserEntity authUser,
            String username,
            String nickname,
            String realname,
            String college,
            String major,
            Integer pageNum,
            Integer pageSize) {

        // 基于用户角色确定信息访问级别
        UserInfoLevel infoLevel = UserInfoLevel.LIMITED; // 默认为有限信息
        if (authUser.isAdmin() || authUser.isSuperAdmin()) {
            infoLevel = UserInfoLevel.ALL; // 管理员可以看到全部信息
        }

        // 默认值处理
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;

        // 创建用户表查询条件
        QueryWrapper<UserEntity> userQuery = new QueryWrapper<>();
        if (StringUtils.hasText(username)) userQuery.like("username", username);
        if (StringUtils.hasText(nickname)) userQuery.like("nickname", nickname);
        if (StringUtils.hasText(realname)) userQuery.like("realname", realname);

        // 处理额外查询条件
        Set<String> matchingUsernames = null;
        Map<String, UserExtraEntity> extraMap = new HashMap<>();

        if (StringUtils.hasText(college) || StringUtils.hasText(major)) {
            QueryWrapper<UserExtraEntity> extraQuery = new QueryWrapper<>();
            if (StringUtils.hasText(college)) extraQuery.like("college", college);
            if (StringUtils.hasText(major)) extraQuery.like("major", major);

            List<UserExtraEntity> extras = userExtraDao.selectList(extraQuery);

            // 为提高后续查找效率，将额外信息放入Map
            extras.forEach(extra -> extraMap.put(extra.getUsername(), extra));

            matchingUsernames = extraMap.keySet();

            // 如果额外条件有匹配，加入用户名条件
            if (!matchingUsernames.isEmpty()) {
                userQuery.in("username", matchingUsernames);
            } else {
                // 如果没有匹配的额外信息，返回空结果
                return new Page<>(pageNum, pageSize);
            }
        }

        // 使用 MyBatis-Plus 的分页查询
        Page<UserEntity> page = new Page<>(pageNum, pageSize, true);
        Page<UserEntity> result = userDao.selectPage(page, userQuery);

        // 处理查询结果
        List<UserEntity> processedRecords = new ArrayList<>();

        for (UserEntity user : result.getRecords()) {
            // 根据权限获取可见的用户信息
            UserEntity filteredUser = null;

            if (infoLevel == UserInfoLevel.ALL) {
                // 管理员可以看到完整信息
                filteredUser = user;
            } else {
                // 非管理员看到有限信息
                filteredUser = new UserEntity(
                        user.getUsername(),
                        user.getNickname(),
                        user.getAvatar(),
                        user.getLevel() != null ? user.getLevel() : "Lv.0"
                );
            }

            // 加载并设置用户额外信息
            UserExtraEntity extra = null;

            if (extraMap.containsKey(user.getUsername())) {
                // 如果在前面查询中已找到
                extra = extraMap.get(user.getUsername());
            } else {
                // 否则单独查询
                extra = userExtraDao.selectOne(
                        new QueryWrapper<UserExtraEntity>().eq("username", user.getUsername())
                );

                // 将新查询到的额外信息也加入map便于后续使用
                if (extra != null) {
                    extraMap.put(user.getUsername(), extra);
                }
            }

            // 设置额外信息
            filteredUser.setUserExtraEntity(extra);

            processedRecords.add(filteredUser);
        }

        // 用处理后的记录替换原始记录
        result.setRecords(processedRecords);

        return result;
    }
}
