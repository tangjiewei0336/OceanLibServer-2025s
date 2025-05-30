package com.oriole.ocean.service;

import com.oriole.ocean.common.po.mysql.UserEntity;
import com.oriole.ocean.common.po.mysql.UserExtraEntity;
import com.oriole.ocean.common.vo.AuthUserEntity;
import com.oriole.ocean.dao.UserDao;
import com.oriole.ocean.dao.UserExtraDao;
import com.oriole.ocean.service.base.UserBaseInfoServiceImpl;
import com.oriole.ocean.service.base.UserExtraInfoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class updateUserInfoWholeTest {

    @Mock
    private UserBaseInfoServiceImpl userService;

    @Mock
    private UserDao userDao;

    @Mock
    private UserExtraDao userExtraDao;

    @InjectMocks
    private UserInfoServiceImpl userInfoService;

    @Mock
    private UserExtraInfoServiceImpl userExtraService;

    private UserEntity testUser;
    private UserExtraEntity extraEntity;
    private UserEntity updatedInfo;
    private AuthUserEntity superAdmin;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testUser = new UserEntity();
        testUser.setUsername("tester");
        testUser.setPassword("password");
        testUser.setRole("USER");
        testUser.setIsValid((byte)1);
        testUser.setNickname("Test User");

        extraEntity = new UserExtraEntity();
        extraEntity.setUsername("tester");
        extraEntity.setCollege("Computer Science");
        extraEntity.setMajor("Software Engineering");

        testUser.setUserExtraEntity(extraEntity);

        // 创建要更新的信息
        updatedInfo = new UserEntity();
        updatedInfo.setUsername("tester");
        updatedInfo.setRole("ADMIN");  // 尝试修改角色

        UserExtraEntity updatedExtra = new UserExtraEntity();
        updatedExtra.setUsername("tester");
        updatedInfo.setUserExtraEntity(updatedExtra);

        // 创建超级管理员
        superAdmin = new TestAuthUser("superadmin", "SUPERADMIN");
    }

    @Test
    void updateUserInfo_WhenSuperAdminUpdatesUserRole_ShouldSucceed() {
        // 配置Mock行为
        // 仅保留实际使用的模拟行为
        when(userService.getById("tester")).thenReturn(testUser);
        when(userService.updateById(any(UserEntity.class))).thenReturn(true);
        when(userExtraService.saveOrUpdate(any(UserExtraEntity.class))).thenReturn(true);

        // 执行更新
        UserEntity result = userInfoService.updateUserInfo(superAdmin, updatedInfo);

        // 验证结果
        assertEquals("ADMIN", result.getRole());

        // 可以添加验证调用
        verify(userExtraService).saveOrUpdate(any(UserExtraEntity.class));
    }

    // 为测试创建的AuthUserEntity实现
    private static class TestAuthUser extends AuthUserEntity {
        private final String role;

        public TestAuthUser(String username, String role) {
            super(username, role);
            this.role = role;
        }

        @Override
        public Boolean isAdmin() {
            return "ADMIN".equalsIgnoreCase(role) || "SUPERADMIN".equalsIgnoreCase(role);
        }

        @Override
        public Boolean isSuperAdmin() {
            return "SUPERADMIN".equalsIgnoreCase(role);
        }
    }
}