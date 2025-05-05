package com.oriole;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserInfoServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private UserExtraService userExtraService;

    @InjectMocks
    private UserInfoService userInfoService;

    @Test
    void updateUserInfo_WhenSuperAdminUpdatesUserRole_ShouldSucceed() {
        // 准备测试数据  
        AuthUserEntity superAdmin = new AuthUserEntity();
        superAdmin.setUsername("superadmin");
        superAdmin.setRole("SUPERADMIN");

        UserEntity existingUser = new UserEntity();
        existingUser.setUsername("testuser");
        existingUser.setRole("USER");

        UserEntity updatedInfo = new UserEntity();
        updatedInfo.setUsername("testuser");
        updatedInfo.setRole("ADMIN");  // 尝试修改角色  

        UserExtraEntity extraEntity = new UserExtraEntity();
        extraEntity.setUsername("testuser");
        existingUser.setUserExtraEntity(extraEntity);
        updatedInfo.setUserExtraEntity(extraEntity);

        // 配置Mock行为  
        when(userService.getById("testuser")).thenReturn(existingUser);
        when(superAdmin.isSuperAdmin()).thenReturn(true);
        when(superAdmin.isAdmin()).thenReturn(true);

        // 执行测试  
        UserEntity result = userInfoService.updateUserInfo(superAdmin, updatedInfo);

        // 验证结果  
        assertEquals("ADMIN", result.getRole());
        verify(userService).updateById(existingUser);
        verify(userExtraService).saveOrUpdate(extraEntity);
    }

    @Test
    void updateUserInfo_WhenNormalAdminUpdatesUserRole_ShouldThrowException() {
        // 准备测试数据  
        AuthUserEntity admin = new AuthUserEntity();
        admin.setUsername("admin");
        admin.setRole("ADMIN");

        UserEntity existingUser = new UserEntity();
        existingUser.setUsername("testuser");
        existingUser.setRole("USER");

        UserEntity updatedInfo = new UserEntity();
        updatedInfo.setUsername("testuser");
        updatedInfo.setRole("ADMIN");  // 尝试修改角色  

        UserExtraEntity extraEntity = new UserExtraEntity();
        extraEntity.setUsername("testuser");
        existingUser.setUserExtraEntity(extraEntity);
        updatedInfo.setUserExtraEntity(extraEntity);

        // 配置Mock行为  
        when(userService.getById("testuser")).thenReturn(existingUser);
        when(admin.isSuperAdmin()).thenReturn(false);
        when(admin.isAdmin()).thenReturn(true);

        // 执行测试并验证异常  
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userInfoService.updateUserInfo(admin, updatedInfo);
        });

        assertEquals("-6", exception.getCode());
        assertEquals("无权限修改用户角色", exception.getMessage());
    }

    @Test
    void updateUserInfo_WhenAdminUpdatesSuperAdminInfo_ShouldThrowException() {
        // 准备测试数据  
        AuthUserEntity admin = new AuthUserEntity();
        admin.setUsername("admin");
        admin.setRole("ADMIN");

        UserEntity existingSuperAdmin = new UserEntity();
        existingSuperAdmin.setUsername("superadmin");
        existingSuperAdmin.setRole("SUPERADMIN");

        UserEntity updatedInfo = new UserEntity();
        updatedInfo.setUsername("superadmin");
        updatedInfo.setNickname("New Name");

        UserExtraEntity extraEntity = new UserExtraEntity();
        extraEntity.setUsername("superadmin");
        existingSuperAdmin.setUserExtraEntity(extraEntity);
        updatedInfo.setUserExtraEntity(extraEntity);

        // 配置Mock行为  
        when(userService.getById("superadmin")).thenReturn(existingSuperAdmin);
        when(admin.isSuperAdmin()).thenReturn(false);
        when(admin.isAdmin()).thenReturn(true);

        // 执行测试并验证异常  
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userInfoService.updateUserInfo(admin, updatedInfo);
        });

        assertEquals("-5", exception.getCode());
        assertEquals("无权限修改超级管理员信息", exception.getMessage());
    }

    @Test
    void updateUserInfo_WhenAdminModifiesUserNickname_ShouldSucceed() {
        // 准备测试数据  
        AuthUserEntity admin = new AuthUserEntity();
        admin.setUsername("admin");
        admin.setRole("ADMIN");

        UserEntity existingUser = new UserEntity();
        existingUser.setUsername("testuser");
        existingUser.setRole("USER");
        existingUser.setNickname("Original Name");

        UserEntity updatedInfo = new UserEntity();
        updatedInfo.setUsername("testuser");
        updatedInfo.setNickname("New Name");

        UserExtraEntity extraEntity = new UserExtraEntity();
        extraEntity.setUsername("testuser");
        existingUser.setUserExtraEntity(extraEntity);
        updatedInfo.setUserExtraEntity(extraEntity);

        // 配置Mock行为  
        when(userService.getById("testuser")).thenReturn(existingUser);
        when(admin.isSuperAdmin()).thenReturn(false);
        when(admin.isAdmin()).thenReturn(true);

        // 执行测试  
        UserEntity result = userInfoService.updateUserInfo(admin, updatedInfo);

        // 验证结果  
        assertEquals("New Name", result.getNickname());
        verify(userService).updateById(existingUser);
        verify(userExtraService).saveOrUpdate(extraEntity);
    }

    @Test
    void updateUserInfo_WhenUserExtraInfoIsNull_ShouldThrowException() {
        // 准备测试数据  
        AuthUserEntity admin = new AuthUserEntity();
        admin.setUsername("admin");
        admin.setRole("ADMIN");

        UserEntity existingUser = new UserEntity();
        existingUser.setUsername("testuser");
        existingUser.setRole("USER");
        existingUser.setUserExtraEntity(new UserExtraEntity());

        UserEntity updatedInfo = new UserEntity();
        updatedInfo.setUsername("testuser");
        updatedInfo.setNickname("New Name");
        updatedInfo.setUserExtraEntity(null);  // 用户附加信息为null  

        // 配置Mock行为  
        when(userService.getById("testuser")).thenReturn(existingUser);
        when(admin.isSuperAdmin()).thenReturn(false);
        when(admin.isAdmin()).thenReturn(true);

        // 执行测试并验证异常  
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userInfoService.updateUserInfo(admin, updatedInfo);
        });

        assertEquals("-3", exception.getCode());
        assertEquals("用户附加信息为空", exception.getMessage());
    }

    @Test
    void updateUserInfo_WhenNonAdminUser_ShouldThrowException() {
        // 准备测试数据  
        AuthUserEntity normalUser = new AuthUserEntity();
        normalUser.setUsername("normaluser");
        normalUser.setRole("USER");

        UserEntity existingUser = new UserEntity();
        existingUser.setUsername("testuser");
        existingUser.setRole("USER");

        UserEntity updatedInfo = new UserEntity();
        updatedInfo.setUsername("testuser");
        updatedInfo.setNickname("New Name");

        // 配置Mock行为  
        when(userService.getById("testuser")).thenReturn(existingUser);
        when(normalUser.isAdmin()).thenReturn(false);

        // 执行测试并验证异常  
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userInfoService.updateUserInfo(normalUser, updatedInfo);
        });

        assertEquals("-2", exception.getCode());
        assertEquals("无权限更新此信息", exception.getMessage());
    }
}