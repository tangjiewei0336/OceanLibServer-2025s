package com.oriole.ocean.service;

import com.oriole.ocean.common.po.mysql.UserEntity;
import com.oriole.ocean.common.po.mysql.UserExtraEntity;
import com.oriole.ocean.common.vo.AuthUserEntity;
import com.oriole.ocean.common.vo.BusinessException;
import com.oriole.ocean.service.base.UserBaseInfoServiceImpl;
import com.oriole.ocean.service.base.UserCertificationServiceImpl;
import com.oriole.ocean.service.base.UserExtraInfoServiceImpl;
import com.oriole.ocean.service.base.UserWalletServiceImpl;
import com.oriole.ocean.dao.UserDao;
import com.oriole.ocean.dao.UserExtraDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class updateUserInfoTest {

    @Mock
    private UserBaseInfoServiceImpl userService;

    @Mock
    private UserExtraInfoServiceImpl userExtraService;

    @Mock
    private UserCertificationServiceImpl userCertificationService;

    @Mock
    private UserWalletServiceImpl walletService;

    @Mock
    private UserDao userDao;

    @Mock
    private UserExtraDao userExtraDao;

    @InjectMocks
    private UserInfoServiceImpl userInfoService;

    @Test
    void updateUserInfo_WhenSuperAdminUpdatesUserRole_ShouldSucceed() {
        // 准备测试数据
        AuthUserEntity superAdmin = mock(AuthUserEntity.class);
        when(superAdmin.isSuperAdmin()).thenReturn(true);
        when(superAdmin.isAdmin()).thenReturn(true);

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
        AuthUserEntity admin = mock(AuthUserEntity.class);

        // 使用lenient()标记可能不会被调用的存根
        lenient().when(admin.isAdmin()).thenReturn(true);

        UserEntity existingUser = new UserEntity();
        existingUser.setUsername("testuser");
        existingUser.setRole("USER");

        UserEntity updatedInfo = new UserEntity();
        updatedInfo.setUsername("testuser");
        updatedInfo.setRole("ADMIN");

        UserExtraEntity extraEntity = new UserExtraEntity();
        extraEntity.setUsername("testuser");
        existingUser.setUserExtraEntity(extraEntity);
        updatedInfo.setUserExtraEntity(extraEntity);

        // 也可能需要对这个存根使用lenient()
        lenient().when(userService.getById("testuser")).thenReturn(existingUser);

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
        AuthUserEntity admin = mock(AuthUserEntity.class);
        when(admin.isSuperAdmin()).thenReturn(false);
        when(admin.isAdmin()).thenReturn(true);

        UserEntity existingSuperAdmin = new UserEntity();
        existingSuperAdmin.setUsername("superadmin");
        existingSuperAdmin.setRole("superadmin");  // 注意这里是小写的superadmin

        UserEntity updatedInfo = new UserEntity();
        updatedInfo.setUsername("superadmin");
        updatedInfo.setNickname("New Name");

        UserExtraEntity extraEntity = new UserExtraEntity();
        extraEntity.setUsername("superadmin");
        existingSuperAdmin.setUserExtraEntity(extraEntity);
        updatedInfo.setUserExtraEntity(extraEntity);

        // 配置Mock行为
        when(userService.getById("superadmin")).thenReturn(existingSuperAdmin);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userInfoService.updateUserInfo(admin, updatedInfo);
        });

        assertEquals("-5", exception.getCode());
        assertEquals("无权限修改超级管理员信息", exception.getMessage());
    }

    @Test
    void updateUserInfo_WhenAdminModifiesUserNickname_ShouldSucceed() {
        // 准备测试数据 - 使用mock()创建模拟对象
        AuthUserEntity admin = mock(AuthUserEntity.class);
        when(admin.isSuperAdmin()).thenReturn(false);
        when(admin.isAdmin()).thenReturn(true);

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
        AuthUserEntity admin = mock(AuthUserEntity.class);
        when(admin.isSuperAdmin()).thenReturn(false);
        when(admin.isAdmin()).thenReturn(true);

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
        AuthUserEntity normalUser = mock(AuthUserEntity.class);
        when(normalUser.isAdmin()).thenReturn(false);

        UserEntity existingUser = new UserEntity();
        existingUser.setUsername("testuser");
        existingUser.setRole("USER");

        UserEntity updatedInfo = new UserEntity();
        updatedInfo.setUsername("testuser");
        updatedInfo.setNickname("New Name");

        // 配置Mock行为
        when(userService.getById("testuser")).thenReturn(existingUser);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userInfoService.updateUserInfo(normalUser, updatedInfo);
        });

        assertEquals("-2", exception.getCode());
        assertEquals("无权限更新此信息", exception.getMessage());
    }

    @Test
    void updateUserInfo_WhenUserDoesNotExist_ShouldThrowException() {
        // 准备测试数据
        AuthUserEntity admin = mock(AuthUserEntity.class);

        UserEntity updatedInfo = new UserEntity();
        updatedInfo.setUsername("nonexistentuser");
        updatedInfo.setNickname("New Name");

        // 配置Mock行为
        when(userService.getById("nonexistentuser")).thenReturn(null);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userInfoService.updateUserInfo(admin, updatedInfo);
        });

        assertEquals("-1", exception.getCode());
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    void updateUserInfo_WhenSuperAdminUpdatesBanStatus_ShouldSucceed() {
        // 准备测试数据
        AuthUserEntity superAdmin = mock(AuthUserEntity.class);
        when(superAdmin.isSuperAdmin()).thenReturn(true);
        when(superAdmin.isAdmin()).thenReturn(true);

        UserEntity existingUser = new UserEntity();
        existingUser.setUsername("testuser");
        existingUser.setRole("USER");
        existingUser.setIsValid((byte)1);

        UserEntity updatedInfo = new UserEntity();
        updatedInfo.setUsername("testuser");
        updatedInfo.setIsValid((byte)0);  // 将用户禁用

        UserExtraEntity extraEntity = new UserExtraEntity();
        extraEntity.setUsername("testuser");
        existingUser.setUserExtraEntity(extraEntity);
        updatedInfo.setUserExtraEntity(extraEntity);

        // 配置Mock行为
        when(userService.getById("testuser")).thenReturn(existingUser);

        // 执行测试
        UserEntity result = userInfoService.updateUserInfo(superAdmin, updatedInfo);

        // 验证结果
        assertEquals((byte)0, result.getIsValid());
        verify(userService).updateById(existingUser);
        verify(userExtraService).saveOrUpdate(extraEntity);
    }

    @Test
    void updateUserInfo_WhenAdminBansAdminUser_ShouldThrowException() {
        // 准备测试数据
        AuthUserEntity admin = mock(AuthUserEntity.class);
        when(admin.isSuperAdmin()).thenReturn(false);
        when(admin.isAdmin()).thenReturn(true);

        UserEntity existingAdmin = new UserEntity();
        existingAdmin.setUsername("otheradmin");
        existingAdmin.setRole("admin");  // 注意这里用小写的admin
        existingAdmin.setIsValid((byte)1);

        UserEntity updatedInfo = new UserEntity();
        updatedInfo.setUsername("otheradmin");
        updatedInfo.setIsValid((byte)-1);  // 尝试禁用另一个管理员

        UserExtraEntity extraEntity = new UserExtraEntity();
        extraEntity.setUsername("otheradmin");
        existingAdmin.setUserExtraEntity(extraEntity);
        updatedInfo.setUserExtraEntity(extraEntity);

        // 配置Mock行为
        when(userService.getById("otheradmin")).thenReturn(existingAdmin);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userInfoService.updateUserInfo(admin, updatedInfo);
        });

        assertEquals("-4", exception.getCode());
        assertEquals("无权限封禁管理员或超级管理员", exception.getMessage());
    }
}