package com.oriole.ocean.service.base;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

    public UserEntity banUser(String username) {
        UserEntity userEntity = getById(username);
        if (userEntity == null) {
            throw new BusinessException("-1", "User not found");
        }

        // 设置用户状态为封禁，例如 is_valid = -1
        userEntity.setIsValid((byte) -1);

        // 更新用户信息
        updateById(userEntity); // 假设这个方法会更新用户信息
        return userEntity; // 返回被封禁的用户信息
    }
}
