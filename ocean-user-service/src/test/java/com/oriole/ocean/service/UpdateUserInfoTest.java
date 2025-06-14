package com.oriole.ocean.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.oriole.ocean.common.po.mysql.UserEntity;
import com.oriole.ocean.common.po.mysql.UserExtraEntity;
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
        // 设置超级管理员权限的用户
        authUser = new AuthUserEntity("adminUsername", "SUPERADMIN");
        // authUser.setSuperAdmin(true);

        // 初始化要更新的用户信息
        updatedInfo = new UserEntity();
        updatedInfo.setUsername("1120210090");
        updatedInfo.setRole("user");
        updatedInfo.setNickname("New Nickname");
        updatedInfo.setEmail("newemail@example.com");
        updatedInfo.setUserExtraEntity(new UserExtraEntity());
    }

    // 连数据库时用
//    @Test
//    public void testUpdateUserInfo_SuperAdmin_UpdatesSuccessfully() {
//        // 模拟已有用户数据
//        UserEntity existingUser = new UserEntity();
//        existingUser.setUsername("testUser");
//        existingUser.setNickname("Old Nickname");
//
//
//        // 设置 updatedInfo
//        UserEntity updatedInfo = new UserEntity();
//        updatedInfo.setUsername("testuser2");
//        updatedInfo.setNickname("Test User");
//
//        // 模拟 userService.getById 方法
//        when(userService.getById("testUser")).thenReturn(existingUser);
//
//        // 设置 authUser 为超级管理员
//        AuthUserEntity authUser = new AuthUserEntity("adminUsername", "ADMIN");
//
//        // 调用更新方法
//        UserEntity result = userInfoService.updateUserInfo(authUser, updatedInfo);
//
//        // 验证调用
//        verify(userService).saveOrUpdate(existingUser); // 确保对 saveOrUpdate 的调用
//        assertEquals("Test User", result.getNickname()); // 确保昵称更新成功
//    }

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


    private AuthUserEntity authUser;
    private UserEntity updatedInfo;


    // 连数据库用
//    @Test
//    public void testUpdateUserInfo_Success() {
//        // 模拟数据库中已有用户数据
//        UserEntity existingUser = new UserEntity();
//        existingUser.setUsername("1120210090");
//        existingUser.setNickname("Old Nickname");
//        existingUser.setRole("user");
//
//        // 设置 updatedInfo
//        UserEntity updatedInfo = new UserEntity();
//        updatedInfo.setUsername("1120210090");
//        updatedInfo.setNickname("New Nickname"); // 设置新昵称
//        updatedInfo.setEmail("newemail@example.com");
//
//        // 模拟 userService.getById 方法
//        when(userService.getById("1120210090")).thenReturn(existingUser);
//
//        // 模拟 userService.saveOrUpdate 方法
//        when(userService.saveOrUpdate(any(UserEntity.class))).thenAnswer(invocation -> {
//            UserEntity savedUser = invocation.getArgument(0);
//            existingUser.setNickname(savedUser.getNickname()); // 更新昵称
//            existingUser.setEmail(savedUser.getEmail()); // 更新邮箱
//            return true; // 返回更新成功
//        });
//
//        // 设置 authUser 为超级管理员
//        AuthUserEntity authUser = new AuthUserEntity("adminUsername", "SUPERADMIN");
//
//        // 调用更新方法
//        UserEntity result = userInfoService.updateUserInfo(authUser, updatedInfo);
//
//        // 验证结果是否符合预期
//        verify(userService).saveOrUpdate(existingUser); // 验证更新用户调用
//        assertEquals("New Nickname", result.getNickname()); // 断言昵称是否更新成功
//        assertEquals("newemail@example.com", result.getEmail()); // 断言邮箱是否更新成功
//    }

    // 连数据库用
//    @Test
//    public void testUpdateUserInfo_Success_2() {
//        // 模拟数据库中已有用户数据
//        UserEntity existingUser = new UserEntity();
//        existingUser.setUsername("1120210090");
//        existingUser.setRole("user");
//        existingUser.setNickname("Old Nickname"); // 设置旧昵称
//
//        // 模拟 userService.getById 方法
//        when(userService.getById("1120210090")).thenReturn(existingUser);
//
//        // 模拟 userService.saveOrUpdate 方法
//        when(userService.saveOrUpdate(any(UserEntity.class))).thenAnswer(invocation -> {
//            UserEntity updatedUser = invocation.getArgument(0);
//            existingUser.setNickname(updatedUser.getNickname()); // 更新昵称
//            return true; // 返回更新成功
//        });
//
//        // 模拟 userExtraService.saveOrUpdate 方法
//        when(userExtraService.saveOrUpdate(any(UserExtraEntity.class))).thenReturn(true);
//
//        // 调用更新方法
//        UserEntity result = userInfoService.updateUserInfo(authUser, updatedInfo);
//
//        // 验证结果是否符合预期
//        verify(userService).saveOrUpdate(existingUser); // 验证更新用户调用
//        verify(userExtraService).saveOrUpdate(any(UserExtraEntity.class)); // 验证更新用户附加信息调用
//        assertEquals("New Nickname", result.getNickname()); // 断言昵称是否更新成功
//    }
}