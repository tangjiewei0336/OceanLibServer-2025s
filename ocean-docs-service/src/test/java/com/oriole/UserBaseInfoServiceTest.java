package com.oriole;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserBaseInfoServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserBaseInfoService userBaseInfoService;

    @Test
    void banUser_WhenSuperAdminBansNormalUser_ShouldSucceed() {
        // 准备测试数据  
        AuthUserEntity superAdmin = new AuthUserEntity();
        superAdmin.setUsername("superadmin");
        superAdmin.setRole("SUPERADMIN");

        UserEntity targetUser = new UserEntity();
        targetUser.setUsername("normaluser");
        targetUser.setRole("USER");
        targetUser.setIsValid(true);

        // 配置Mock行为  
        when(userService.getById("normaluser")).thenReturn(targetUser);
        when(superAdmin.isSuperAdmin()).thenReturn(true);
        when(superAdmin.isAdmin()).thenReturn(true);

        // 执行测试  
        UserEntity result = userBaseInfoService.banUser(superAdmin, "normaluser");

        // 验证结果  
        assertFalse(result.getIsValid());
        verify(userService).updateById(targetUser);
    }

    @Test
    void banUser_WhenAdminBansNormalUser_ShouldSucceed() {
        // 准备测试数据  
        AuthUserEntity admin = new AuthUserEntity();
        admin.setUsername("admin");
        admin.setRole("ADMIN");

        UserEntity targetUser = new UserEntity();
        targetUser.setUsername("normaluser");
        targetUser.setRole("USER");
        targetUser.setIsValid(true);

        // 配置Mock行为  
        when(userService.getById("normaluser")).thenReturn(targetUser);
        when(admin.isSuperAdmin()).thenReturn(false);
        when(admin.isAdmin()).thenReturn(true);

        // 执行测试  
        UserEntity result = userBaseInfoService.banUser(admin, "normaluser");

        // 验证结果  
        assertFalse(result.getIsValid());
        verify(userService).updateById(targetUser);
    }

    @Test
    void banUser_WhenAdminBansAdmin_ShouldThrowException() {
        // 准备测试数据  
        AuthUserEntity admin = new AuthUserEntity();
        admin.setUsername("admin1");
        admin.setRole("ADMIN");

        UserEntity targetAdmin = new UserEntity();
        targetAdmin.setUsername("admin2");
        targetAdmin.setRole("ADMIN");
        targetAdmin.setIsValid(true);

        // 配置Mock行为  
        when(userService.getById("admin2")).thenReturn(targetAdmin);
        when(admin.isSuperAdmin()).thenReturn(false);
        when(admin.isAdmin()).thenReturn(true);

        // 执行测试并验证异常  
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userBaseInfoService.banUser(admin, "admin2");
        });

        assertEquals("-4", exception.getCode());
        assertEquals("无权限封禁管理员或超级管理员", exception.getMessage());
    }

    @Test
    void banUser_WhenUserNotFound_ShouldThrowException() {
        // 准备测试数据  
        AuthUserEntity admin = new AuthUserEntity();
        admin.setUsername("admin");
        admin.setRole("ADMIN");

        // 配置Mock行为  
        when(userService.getById("nonexistent")).thenReturn(null);

        // 执行测试并验证异常  
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userBaseInfoService.banUser(admin, "nonexistent");
        });

        assertEquals("-1", exception.getCode());
        assertEquals("用户不存在", exception.getMessage());
    }
}