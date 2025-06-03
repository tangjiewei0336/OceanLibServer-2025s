package com.oriole.ocean.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.oriole.ocean.common.po.mysql.UserEntity;
import com.oriole.ocean.common.vo.AuthUserEntity;
import com.oriole.ocean.common.vo.BusinessException;
import com.oriole.ocean.service.base.UserBaseInfoServiceImpl;
import com.oriole.ocean.service.base.UserExtraInfoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UpdateUserInfoTest {

    @Mock
    private UserBaseInfoServiceImpl userService; // 这里引用的是实现类
    @Mock
    private UserExtraInfoServiceImpl userExtraService; // 这里引用的是实现类

    @InjectMocks
    private UserInfoServiceImpl userInfoService; // 被测试的类

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // 初始化 Mockito 注解
    }

    @Test
    public void testUpdateUserInfo_SuperAdmin_UpdatesSuccessfully() {
        // Arrange
        AuthUserEntity superAdmin = new AuthUserEntity("adminUsername", "SUPERADMIN");

        UserEntity existingUser = new UserEntity("testUser", "Test User", "password", (byte) 1, "user");
        when(userService.getById("testUser")).thenReturn(existingUser);

        UserEntity updatedInfo = new UserEntity();
        updatedInfo.setUsername("testUser");
        updatedInfo.setRole("admin"); // Super admin can change role
        updatedInfo.setIsValid((byte) 0); // Super admin can change validity

        // Act
        UserEntity result = userInfoService.updateUserInfo(superAdmin, updatedInfo);

        // Assert
        assertEquals("admin", result.getRole());
        assertEquals((byte) 0, result.getIsValid());
        verify(userService).updateById(existingUser);
    }

    @Test
    public void testUpdateUserInfo_Admin_CannotChangeRole() {
        // Arrange
        AuthUserEntity Admin = new AuthUserEntity("adminUsername", "ADMIN");

        UserEntity existingUser = new UserEntity("testUser", "Test User", "password", (byte) 1, "user");
        when(userService.getById("testUser")).thenReturn(existingUser);

        UserEntity updatedInfo = new UserEntity();
        updatedInfo.setUsername("testUser");
        updatedInfo.setRole("admin"); // Admin should not be able to change role

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userInfoService.updateUserInfo(Admin, updatedInfo);
        });
        assertEquals("-6", exception.getCode());
        assertEquals("无权限修改用户角色", exception.getMessage());
    }

    @Test
    public void testUpdateUserInfo_Admin_CannotBanAdmin() {
        // Arrange
        AuthUserEntity Admin = new AuthUserEntity("adminUsername", "ADMIN");

        UserEntity existingUser = new UserEntity("testUser", "Test User", "password", (byte) 1, "admin");
        when(userService.getById("testUser")).thenReturn(existingUser);

        UserEntity updatedInfo = new UserEntity();
        updatedInfo.setUsername("testUser");
        updatedInfo.setIsValid((byte) 0); // Admin trying to ban another admin

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userInfoService.updateUserInfo(Admin, updatedInfo);
        });
        assertEquals("-4", exception.getCode());
        assertEquals("无权限封禁管理员或超级管理员", exception.getMessage());
    }

    @Test
    public void testUpdateUserInfo_UserNotFound() {
        // Arrange
        AuthUserEntity Admin = new AuthUserEntity("adminUsername", "ADMIN");

        when(userService.getById("nonExistentUser")).thenReturn(null);

        UserEntity updatedInfo = new UserEntity();
        updatedInfo.setUsername("nonExistentUser");

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userInfoService.updateUserInfo(Admin, updatedInfo);
        });
        assertEquals("-1", exception.getCode());
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    public void testUpdateUserInfo_EmptyUserExtra() {
        // Arrange
        AuthUserEntity admin = new AuthUserEntity("adminUsername", "ADMIN");

        UserEntity existingUser = new UserEntity("testUser", "Test User", "password", (byte) 1, "user");
        when(userService.getById("testUser")).thenReturn(existingUser);

        UserEntity updatedInfo = new UserEntity();
        updatedInfo.setUsername("testUser");
        updatedInfo.setUserExtraEntity(null); // No user extra info

        // Act
        UserEntity result = userInfoService.updateUserInfo(admin, updatedInfo);

        // Assert
        assertEquals("testUser", result.getUsername());
        assertEquals("Test User", result.getNickname()); // 确保其他字段未被修改
        // 这里可以添加更多的断言以验证其他字段
    }
}