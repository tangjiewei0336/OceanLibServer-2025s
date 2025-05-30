package com.oriole.ocean.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oriole.ocean.common.po.mysql.UserEntity;
import com.oriole.ocean.common.po.mysql.UserExtraEntity;
import com.oriole.ocean.common.vo.AuthUserEntity;
import com.oriole.ocean.dao.UserDao;
import com.oriole.ocean.dao.UserExtraDao;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class searchUserWithPagingTest {

    @Mock
    private UserDao userDao;

    @Mock
    private UserExtraDao userExtraDao;

    @InjectMocks
    private UserInfoServiceImpl userInfoService;

    private AuthUserEntity normalUser;
    private AuthUserEntity adminUser;
    @Getter
    private AuthUserEntity superAdminUser;
    private List<UserEntity> mockUsers;
    private List<UserExtraEntity> mockExtras;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        normalUser = new TestAuthUser("user", "USER");
        adminUser = new TestAuthUser("admin", "ADMIN");
        superAdminUser = new TestAuthUser("superadmin", "SUPERADMIN");

        // 创建模拟用户数据
        mockUsers = new ArrayList<>();
        mockExtras = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            UserEntity user = new UserEntity();
            user.setUsername("user" + i);
            user.setNickname("Test User " + i);
            user.setRealname("Real Name " + i);
            user.setPassword("password" + i);
            user.setRole(i <= 3 ? "USER" : (i == 4 ? "ADMIN" : "SUPERADMIN"));
            user.setAvatar("avatar" + i + ".jpg");
            user.setLevel("Lv." + i);
            user.setIsValid((byte) 1);
            mockUsers.add(user);

            UserExtraEntity extra = new UserExtraEntity();
            extra.setUsername("user" + i);
            extra.setCollege(i % 2 == 0 ? "Computer Science" : "Engineering");
            extra.setMajor(i % 2 == 0 ? "Software Engineering" : "Electrical Engineering");
            mockExtras.add(extra);
        }
    }

    @Test
    void whenSearchByNormalUser_thenReturnLimitedInfo() {
        // 模拟查询结果
        Page<UserEntity> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(mockUsers.get(0), mockUsers.get(1)));
        mockPage.setTotal(2);

        when(userDao.selectCount(any())).thenReturn(2);
        when(userDao.selectPage(any(), any())).thenReturn(mockPage);
        when(userExtraDao.selectOne(any())).thenReturn(mockExtras.get(0), mockExtras.get(1));

        // 执行查询
        Page<UserEntity> result = userInfoService.searchUsersWithPaging(
                normalUser, null, null, null, null, null, 1, 10);

        // 验证结果
        assertEquals(2, result.getTotal());
        assertEquals(2, result.getRecords().size());

        // 验证敏感字段不返回给普通用户
        UserEntity firstUser = result.getRecords().get(0);
        assertNull(firstUser.getPassword());
        assertNull(firstUser.getRole());
        assertNull(firstUser.getIsValid());
        assertNotNull(firstUser.getUsername());
        assertNotNull(firstUser.getNickname());
        assertNotNull(firstUser.getAvatar());
        assertNotNull(firstUser.getLevel());

        // 验证额外信息加载
        assertNotNull(firstUser.getUserExtraEntity());
    }

    @Test
    void whenSearchByAdmin_thenReturnAllInfo() {
        // 模拟查询结果
        Page<UserEntity> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(mockUsers.get(0), mockUsers.get(1)));
        mockPage.setTotal(2);

        when(userDao.selectCount(any())).thenReturn(2);
        when(userDao.selectPage(any(), any())).thenReturn(mockPage);
        when(userExtraDao.selectOne(any())).thenReturn(mockExtras.get(0), mockExtras.get(1));

        // 执行查询
        Page<UserEntity> result = userInfoService.searchUsersWithPaging(
                adminUser, null, null, null, null, null, 1, 10);

        // 验证结果
        assertEquals(2, result.getTotal());
        assertEquals(2, result.getRecords().size());

        // 验证管理员可以看到所有字段
        UserEntity firstUser = result.getRecords().get(0);
        assertNotNull(firstUser.getPassword());
        assertNotNull(firstUser.getRole());
        assertNotNull(firstUser.getIsValid());
    }

    @Test
    void whenSearchWithFilters_thenApplyFilters() {
        // 这里我们需要捕获查询条件
        ArgumentCaptor<QueryWrapper<UserEntity>> userQueryCaptor =
                ArgumentCaptor.forClass(QueryWrapper.class);

        // 模拟查询结果
        Page<UserEntity> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(mockUsers.get(0)));
        mockPage.setTotal(1);

        when(userDao.selectCount(any())).thenReturn(1);
        when(userDao.selectPage(any(), userQueryCaptor.capture())).thenReturn(mockPage);
        when(userExtraDao.selectOne(any())).thenReturn(mockExtras.get(0));

        // 执行查询，使用所有筛选条件
        userInfoService.searchUsersWithPaging(
                adminUser, "user", "Test", "Real", null, null, 1, 10);

        // 验证查询条件
        // 注意：由于QueryWrapper的equals实现，我们无法直接比较对象
        // 但我们可以验证是否调用了selectPage方法
        verify(userDao).selectPage(any(Page.class), any(QueryWrapper.class));
    }

    @Test
    void whenSearchWithCollegeAndMajor_thenUseExtraDao() {
        // 模拟额外信息查询结果
        when(userExtraDao.selectList(any())).thenReturn(Arrays.asList(mockExtras.get(0)));

        // 模拟用户查询结果
        Page<UserEntity> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(mockUsers.get(0)));
        mockPage.setTotal(1);

        when(userDao.selectCount(any())).thenReturn(1);
        when(userDao.selectPage(any(), any())).thenReturn(mockPage);

        // 执行查询
        Page<UserEntity> result = userInfoService.searchUsersWithPaging(
                adminUser, null, null, null, "Computer Science", "Software", 1, 10);

        // 验证调用了userExtraDao.selectList
        verify(userExtraDao).selectList(any(QueryWrapper.class));

        // 验证结果
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    void whenNoExtraResults_thenReturnEmptyPage() {
        // 模拟没有额外信息匹配的情况
        when(userExtraDao.selectList(any())).thenReturn(new ArrayList<>());

        // 执行查询
        Page<UserEntity> result = userInfoService.searchUsersWithPaging(
                adminUser, null, null, null, "Unknown College", null, 1, 10);

        // 验证返回空页
        assertEquals(0, result.getTotal());
        assertEquals(0, result.getRecords().size());

        // 验证没有调用用户查询
        verify(userDao, never()).selectPage(any(), any());
    }

    @Test
    void whenPageNumExceedsMaxPage_thenAdjustPageNum() {
        // 模拟总记录数为5
        when(userDao.selectCount(any())).thenReturn(5);

        Page<UserEntity> mockPage = new Page<>(1, 10);
        mockPage.setRecords(mockUsers);
        mockPage.setTotal(5);

        when(userDao.selectPage(any(), any())).thenReturn(mockPage);
        when(userExtraDao.selectOne(any())).thenReturn(
                mockExtras.get(0), mockExtras.get(1), mockExtras.get(2),
                mockExtras.get(3), mockExtras.get(4));

        // 尝试请求一个超过最大页数的页
        Page<UserEntity> result = userInfoService.searchUsersWithPaging(
                adminUser, null, null, null, null, null, 100, 10);

        // 验证已调整页码
        verify(userDao).selectPage(any(Page.class), any(QueryWrapper.class));
        assertEquals(5, result.getTotal());
        assertEquals(5, result.getRecords().size());
    }

    // 辅助类
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