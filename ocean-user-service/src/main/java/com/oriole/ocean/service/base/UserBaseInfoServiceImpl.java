package com.oriole.ocean.service.base;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oriole.ocean.common.vo.AuthUserEntity;
import com.oriole.ocean.common.vo.BusinessException;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.dao.UserDao;
import com.oriole.ocean.common.po.mysql.UserEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

@Service
@Transactional
public class UserBaseInfoServiceImpl extends ServiceImpl<UserDao, UserEntity> {

    public UserEntity getUserInfoByUsernameAndPassword(String username, String password) {
        UserEntity userEntity = getById(username);
        if (userEntity == null || !userEntity.getPassword().equals(password)) {
            throw new BusinessException("-2", "用户名或密码错误");
        }
        return userLoginHandler(userEntity);
    }

    private UserEntity userLoginHandler(UserEntity userEntity) {
        if (userEntity.getIsValid().equals((byte) -1)) {
            throw new BusinessException("-3", "账号已锁定或被封禁，请联系管理员处理");
        }
        if (userEntity.getIsValid().equals((byte) 0)) {
            throw new BusinessException("-4", "账号需等待管理员审核"); // 刚进入注册认证
        }
        return userEntity;
    }

    // 未使用
    public UserEntity banUser(AuthUserEntity authUser, String username) {
        // 1. 获取执行操作的用户角色
        String operatorRole = authUser.getRole();

        // 2. 获取目标用户信息
        UserEntity targetUser = getById(username);
        if (targetUser == null) {
            throw new BusinessException("-1", "User not found");
        }

        // 3. 获取目标用户角色
        // 假设用户实体中有 role 字段，或者通过其他方式获取
        String targetRole = targetUser.getRole();

        // 4. 权限检查
        if (!operatorRole.equals("ADMIN") && !operatorRole.equals("SUPERADMIN")) {
            throw new BusinessException("-2", "Permission denied: Only administrators can ban users");
        }

        // 管理员试图封禁超级管理员
        if (operatorRole.equals("ADMIN") && targetRole.equals("SUPERADMIN")) {
            throw new BusinessException("-2", "Permission denied: Cannot ban SUPERADMIN");
        }

        // 管理员试图封禁其他管理员
        if (operatorRole.equals("ADMIN") && targetRole.equals("ADMIN")) {
            throw new BusinessException("-2", "Permission denied: Administrators cannot ban each other");
        }

        // 5. 执行封禁操作
        targetUser.setIsValid((byte) -1);
        updateById(targetUser);

        return targetUser;
    }
}
