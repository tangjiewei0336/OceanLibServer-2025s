package com.oriole.ocean.service;

import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oriole.ocean.common.enumerate.UserInfoLevel;
import com.oriole.ocean.common.po.mysql.UserEntity;
import com.oriole.ocean.common.po.mysql.UserExtraEntity;
import com.oriole.ocean.common.service.UserInfoService;
import com.oriole.ocean.common.vo.AuthUserEntity;
import com.oriole.ocean.common.vo.BusinessException;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.dao.UserDao;
import com.oriole.ocean.dao.UserExtraDao;
import com.oriole.ocean.service.base.UserBaseInfoServiceImpl;
import com.oriole.ocean.service.base.UserCertificationServiceImpl;
import com.oriole.ocean.service.base.UserExtraInfoServiceImpl;
import com.oriole.ocean.service.base.UserWalletServiceImpl;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

@Service
@DubboService
public class UserInfoServiceImpl implements UserInfoService {
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
        // 检查用户是否存在
        UserEntity userEntity = userService.getById(updatedInfo.getUsername());
        if (userEntity == null) {
            throw new BusinessException("-1", "用户不存在");
        }

        // 超级管理员可以修改所有信息
        if (authUser.isSuperAdmin()) {
            // 超级管理员独有权限：修改角色
            if (updatedInfo.getRole() != null) {
                userEntity.setRole(updatedInfo.getRole());
            }

            // 修改用户有效性（超级管理员无限制）
            if (updatedInfo.getIsValid() != null) {
                userEntity.setIsValid(updatedInfo.getIsValid());
            }

            // 超级管理员也能修改所有其他信息
            updateBasicUserInfo(userEntity, updatedInfo);

        } else if (authUser.isAdmin()) {
            // 管理员不能修改角色
            if (updatedInfo.getRole() != null) {
                throw new BusinessException("-6", "无权限修改用户角色");
            }

            // 管理员修改 isValid 需要检查目标用户角色
            if (updatedInfo.getIsValid() != null) {
                if (userEntity.getRole() != null &&
                        (userEntity.getRole().equals("admin") || userEntity.getRole().equals("superadmin"))) {
                    throw new BusinessException("-4", "无权限封禁管理员或超级管理员");
                }
                userEntity.setIsValid(updatedInfo.getIsValid());
            }

            // 管理员可以修改其他基本信息
            updateBasicUserInfo(userEntity, updatedInfo);

        } else {
            throw new BusinessException("-2", "无权限更新此信息");
        }

        // 更新用户附加信息
        updateUserExtraInfo(userEntity, updatedInfo);

        // 同时更新用户信息和用户附加信息到数据库
        boolean userUpdateResult = userService.saveOrUpdate(userEntity);
        boolean extraUpdateResult = updateUserExtraInfoToDb(userEntity);

        if (!userUpdateResult) {
            System.out.print("User update did not affect any rows in the database.");
        }
        if (!extraUpdateResult) {
            System.out.print("User extra update did not affect any rows in the database.");
        }

        return userEntity;
    }

    /**
     * 更新用户基本信息（昵称、手机号等）
     */
    private void updateBasicUserInfo(UserEntity userEntity, UserEntity updatedInfo) {
        if (updatedInfo.getNickname() != null) {
            System.out.println("Old User Nickname: " + userEntity.getNickname());
            System.out.println("UpdateInfo's Nickname: " + updatedInfo.getNickname());
            userEntity.setNickname(updatedInfo.getNickname());
            System.out.println("Updating User Nickname: " + userEntity.getNickname());
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
    }

    /**
     * 更新用户附加信息
     */
    private void updateUserExtraInfo(UserEntity userEntity, UserEntity updatedInfo) {
        UserExtraEntity tempUserExtraEntity = userEntity.getUserExtraEntity();
        if (tempUserExtraEntity == null) {
            tempUserExtraEntity = new UserExtraEntity();
            tempUserExtraEntity.setUsername(userEntity.getUsername());
            userEntity.setUserExtraEntity(tempUserExtraEntity);
        }

        UserExtraEntity updateInfoExtraEntity = updatedInfo.getUserExtraEntity();
        if (updateInfoExtraEntity != null) {
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
        }
    }

    /**
     * 将用户附加信息保存到数据库
     */
    private boolean updateUserExtraInfoToDb(UserEntity userEntity) {
        UserExtraEntity userExtraEntity = userEntity.getUserExtraEntity();
        if (userExtraEntity != null) {
            return userExtraService.saveOrUpdate(userExtraEntity);
        }
        return true; // 如果没有附加信息需要更新，认为成功
    }


    /**
     * 未分页搜索用户信息
     * @return 用户信息的 List。未使用
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
        UserInfoLevel infoLevel = UserInfoLevel.LIMITED;
        if (authUser.isAdmin() || authUser.isSuperAdmin()) {
            infoLevel = UserInfoLevel.ALL;
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
        Map<String, UserExtraEntity> extraMap = new HashMap<>();

        if (StringUtils.hasText(college) || StringUtils.hasText(major)) {
            QueryWrapper<UserExtraEntity> extraQuery = new QueryWrapper<>();
            if (StringUtils.hasText(college)) extraQuery.like("college", college);
            if (StringUtils.hasText(major)) extraQuery.like("major", major);

            List<UserExtraEntity> extras = userExtraDao.selectList(extraQuery);
            extras.forEach(extra -> extraMap.put(extra.getUsername(), extra));

            if (!extraMap.isEmpty()) {
                userQuery.in("username", extraMap.keySet());
            } else {
                // 创建空结果页
                Page<UserEntity> emptyPage = new Page<>(pageNum, pageSize);
                emptyPage.setTotal(0);
                emptyPage.setRecords(new ArrayList<>());
                return emptyPage;
            }
        }

        // 计算总数并调整页码
        long count = userDao.selectCount(userQuery);
        long maxPage = (count + pageSize - 1) / pageSize;

        if (maxPage > 0 && pageNum > maxPage) {
            pageNum = (int) maxPage;
        }

        // 执行分页查询
        Page<UserEntity> page = new Page<>(pageNum, pageSize, true);
        Page<UserEntity> result = userDao.selectPage(page, userQuery);

        // 处理结果，加载额外信息
        List<UserEntity> processedRecords = new ArrayList<>();

        for (UserEntity user : result.getRecords()) {
            // 根据权限过滤
            UserEntity filteredUser = infoLevel == UserInfoLevel.ALL ?
                    user :
                    new UserEntity(
                            user.getUsername(),
                            user.getNickname(),
                            user.getAvatar(),
                            user.getLevel() != null ? user.getLevel() : "Lv.0"
                    );

            // 加载额外信息
            UserExtraEntity extra = extraMap.getOrDefault(
                    user.getUsername(),
                    userExtraDao.selectOne(new QueryWrapper<UserExtraEntity>().eq("username", user.getUsername()))
            );

            filteredUser.setUserExtraEntity(extra);
            processedRecords.add(filteredUser);
        }

        // 更新结果记录
        result.setRecords(processedRecords);

        return result;
    }
}
