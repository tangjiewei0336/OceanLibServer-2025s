/*
package com.oriole.ocean.service.base;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oriole.ocean.common.po.mysql.UserEntity;
import com.oriole.ocean.common.vo.AuthUserEntity;
import com.oriole.ocean.common.vo.BusinessException;
import com.oriole.ocean.dao.UserDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserBaseInfoServiceTest {

    @Mock
    private UserDao userDao;

    @Spy
    @InjectMocks
    private UserBaseInfoServiceImpl userBaseInfoService;

    // ===== banUser方法测试 =====

    @Test
    void banUser_WhenSuperAdminBansNormalUser_ShouldSucceed() {
        // 准备测试数据
        AuthUserEntity superAdmin = new AuthUserEntity("superadmin", "SUPERADMIN");

        UserEntity targetUser = new UserEntity();
        targetUser.setUsername("normaluser");
        targetUser.setRole("USER");
        targetUser.setIsValid((byte) 1);

        // 配置Mock行为
        doReturn(targetUser).when(userBaseInfoService).getById("normaluser");
        doNothing().when(userBaseInfoService).updateById(any(UserEntity.class));

        // 执行测试
        UserEntity result = userBaseInfoService.banUser(superAdmin, "normaluser");

        // 验证结果
        assertEquals((byte) -1, result.getIsValid());
        verify(userBaseInfoService).updateById(targetUser);
    }

    @Test
    void banUser_WhenAdminBansNormalUser_ShouldSucceed() {
        // 准备测试数据
        AuthUserEntity admin = new AuthUserEntity("admin", "ADMIN");

        UserEntity targetUser = new UserEntity();
        targetUser.setUsername("normaluser");
        targetUser.setRole("USER");
        targetUser.setIsValid((byte) 1);

        // 配置Mock行为
        doReturn(targetUser).when(userBaseInfoService).getById("normaluser");
        doNothing().when(userBaseInfoService).updateById(any(UserEntity.class));

        // 执行测试
        UserEntity result = userBaseInfoService.banUser(admin, "normaluser");

        // 验证结果
        assertEquals((byte) -1, result.getIsValid());
        verify(userBaseInfoService).updateById(targetUser);
    }

    @Test
    void banUser_WhenAdminBansAdmin_ShouldThrowException() {
        // 准备测试数据
        AuthUserEntity admin = new AuthUserEntity("admin1", "ADMIN");

        UserEntity targetAdmin = new UserEntity();
        targetAdmin.setUsername("admin2");
        targetAdmin.setRole("ADMIN");
        targetAdmin.setIsValid((byte) 1);

        // 配置Mock行为
        doReturn(targetAdmin).when(userBaseInfoService).getById("admin2");

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userBaseInfoService.banUser(admin, "admin2");
        });

        assertEquals("-2", exception.getCode());
        assertEquals("Permission denied: Administrators cannot ban each other", exception.getMessage());
    }

    @Test
    void banUser_WhenAdminBansSuperAdmin_ShouldThrowException() {
        // 准备测试数据
        AuthUserEntity admin = new AuthUserEntity("admin", "ADMIN");

        UserEntity superAdmin = new UserEntity();
        superAdmin.setUsername("superadmin");
        superAdmin.setRole("SUPERADMIN");
        superAdmin.setIsValid((byte) 1);

        // 配置Mock行为
        doReturn(superAdmin).when(userBaseInfoService).getById("superadmin");

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userBaseInfoService.banUser(admin, "superadmin");
        });

        assertEquals("-2", exception.getCode());
        assertEquals("Permission denied: Cannot ban SUPERADMIN", exception.getMessage());
    }

    @Test
    void banUser_WhenSuperAdminBansAdmin_ShouldSucceed() {
        // 准备测试数据
        AuthUserEntity superAdmin = new AuthUserEntity("superadmin", "SUPERADMIN");

        UserEntity adminUser = new UserEntity();
        adminUser.setUsername("admin");
        adminUser.setRole("ADMIN");
        adminUser.setIsValid((byte) 1);

        // 配置Mock行为
        doReturn(adminUser).when(userBaseInfoService).getById("admin");
        doNothing().when(userBaseInfoService).updateById(any(UserEntity.class));

        // 执行测试
        UserEntity result = userBaseInfoService.banUser(superAdmin, "admin");

        // 验证结果
        assertEquals((byte) -1, result.getIsValid());
        verify(userBaseInfoService).updateById(adminUser);
    }

    @Test
    void banUser_WhenNormalUserTriesToBan_ShouldThrowException() {
        // 准备测试数据
        AuthUserEntity normalUser = new AuthUserEntity("user1", "USER");

        UserEntity targetUser = new UserEntity();
        targetUser.setUsername("user2");
        targetUser.setRole("USER");
        targetUser.setIsValid((byte) 1);

        // 配置Mock行为
        doReturn(targetUser).when(userBaseInfoService).getById("user2");

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userBaseInfoService.banUser(normalUser, "user2");
        });

        assertEquals("-2", exception.getCode());
        assertEquals("Permission denied: Only administrators can ban users", exception.getMessage());
    }

    @Test
    void banUser_WhenUserNotFound_ShouldThrowException() {
        // 准备测试数据
        AuthUserEntity admin = new AuthUserEntity("admin", "ADMIN");

        // 配置Mock行为
        doReturn(null).when(userBaseInfoService).getById("nonexistent");

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userBaseInfoService.banUser(admin, "nonexistent");
        });

        assertEquals("-1", exception.getCode());
        assertEquals("User not found", exception.getMessage());
    }

    // ===== getUserInfoByUsernameAndPassword方法测试 =====

    @Test
    void getUserInfoByUsernameAndPassword_WhenCorrectCredentials_ShouldReturnUser() {
        // 准备测试数据
        String username = "testuser";
        String password = "password123";

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setPassword(password);
        userEntity.setIsValid((byte) 1);  // 有效账号

        // 配置Mock行为
        doReturn(userEntity).when(userBaseInfoService).getById(username);

        // 执行测试
        UserEntity result = userBaseInfoService.getUserInfoByUsernameAndPassword(username, password);

        // 验证结果
        assertNotNull(result);
        assertEquals(username, result.getUsername());
    }

    @Test
    void getUserInfoByUsernameAndPassword_WhenUserNotFound_ShouldThrowException() {
        // 准备测试数据
        String username = "nonexistent";
        String password = "password123";

        // 配置Mock行为
        doReturn(null).when(userBaseInfoService).getById(username);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userBaseInfoService.getUserInfoByUsernameAndPassword(username, password);
        });

        assertEquals("-2", exception.getCode());
        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    void getUserInfoByUsernameAndPassword_WhenIncorrectPassword_ShouldThrowException() {
        // 准备测试数据
        String username = "testuser";
        String correctPassword = "password123";
        String wrongPassword = "wrongpassword";

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setPassword(correctPassword);
        userEntity.setIsValid((byte) 1);

        // 配置Mock行为
        doReturn(userEntity).when(userBaseInfoService).getById(username);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userBaseInfoService.getUserInfoByUsernameAndPassword(username, wrongPassword);
        });

        assertEquals("-2", exception.getCode());
        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    void getUserInfoByUsernameAndPassword_WhenUserBanned_ShouldThrowException() {
        // 准备测试数据
        String username = "banneduser";
        String password = "password123";

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setPassword(password);
        userEntity.setIsValid((byte) -1);  // 封禁状态

        // 配置Mock行为
        doReturn(userEntity).when(userBaseInfoService).getById(username);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userBaseInfoService.getUserInfoByUsernameAndPassword(username, password);
        });

        assertEquals("-3", exception.getCode());
        assertEquals("账号已锁定或被封禁，请联系管理员处理", exception.getMessage());
    }

    @Test
    void getUserInfoByUsernameAndPassword_WhenUserPendingApproval_ShouldThrowException() {
        // 准备测试数据
        String username = "pendinguser";
        String password = "password123";

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setPassword(password);
        userEntity.setIsValid((byte) 0);  // 待审核状态

        // 配置Mock行为
        doReturn(userEntity).when(userBaseInfoService).getById(username);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userBaseInfoService.getUserInfoByUsernameAndPassword(username, password);
        });

        assertEquals("-4", exception.getCode());
        assertEquals("账号需等待管理员审核", exception.getMessage());
    }
}*/
