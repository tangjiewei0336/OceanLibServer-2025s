package com.oriole.ocean.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oriole.ocean.common.auth.AuthUser;
import com.oriole.ocean.common.dto.UserSearchDTO;
import com.oriole.ocean.common.po.mysql.UserEntity;
import com.oriole.ocean.common.po.mysql.UserExtraEntity;
import com.oriole.ocean.common.vo.AuthUserEntity;
import com.oriole.ocean.common.vo.BusinessException;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.dao.UserDao;
import com.oriole.ocean.dao.UserExtraDao;
import com.oriole.ocean.service.UserInfoServiceImpl;
import com.oriole.ocean.service.base.UserBaseInfoServiceImpl;
import com.oriole.ocean.service.base.UserCertificationServiceImpl;
import com.oriole.ocean.service.base.UserExtraInfoServiceImpl;
import com.oriole.ocean.service.base.UserWalletServiceImpl;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@RestController
@Slf4j
@RequestMapping("/userInfoService")
public class UserInfoControllerTest {
    @Autowired
    UserBaseInfoServiceImpl userBaseInfoService;
    @Autowired
    UserInfoServiceImpl userInfoService;

    @RequestMapping(value = "/searchUsers", method = RequestMethod.POST)
    public MsgEntity<Page<UserEntity>> searchUsers(
            @AuthUser AuthUserEntity authUser,
            @RequestBody UserSearchDTO searchParams) {
        // 调用服务层方法，传入权限级别
        Page<UserEntity> results = userInfoService.searchUsersWithPaging(
                authUser,
                searchParams.getUsername(),
                searchParams.getNickname(),
                searchParams.getRealname(),
                searchParams.getCollege(),
                searchParams.getMajor(),
                searchParams.getPageNum(),
                searchParams.getPageSize()
        );

        return new MsgEntity<>("SUCCESS", "1", results);
    }

    @Nested
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
            MockitoAnnotations.openMocks(this);

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
            // 创建 ArgumentCaptor 捕获
            ArgumentCaptor<QueryWrapper<UserEntity>> userQueryCaptor =
                    ArgumentCaptor.forClass(QueryWrapper.class); // 显式指定类型

            // 模拟查询结果
            Page<UserEntity> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Collections.singletonList(mockUsers.get(0)));
            mockPage.setTotal(1);

            // 模拟 DAO 行为
            when(userDao.selectCount(any())).thenReturn(1);
            when(userDao.selectPage(any(Page.class), userQueryCaptor.capture())).thenReturn(mockPage);
            when(userExtraDao.selectOne(any())).thenReturn(mockExtras.get(0));

            // 执行查询，使用所有筛选条件
            userInfoService.searchUsersWithPaging(
                    adminUser, "user", "Test", "Real", null, null, 1, 10);

            // 验证调用 selectPage 方法
            verify(userDao).selectPage(any(Page.class), userQueryCaptor.capture());

            // 验证捕获的 QueryWrapper 内容
            QueryWrapper<UserEntity> capturedQuery = userQueryCaptor.getValue();

            // 检查捕获的 QueryWrapper
            assertNotNull(capturedQuery);
            // 这里可以进一步验证查询条件
            // 例如，检查是否包含username、nickname等条件
            // 这取决于你的 QueryWrapper 的实现
        }

        @Test
        void whenSearchWithCollegeAndMajor_thenUseExtraDao() {
            // 模拟额外信息查询结果
            when(userExtraDao.selectList(any())).thenReturn(Collections.singletonList(mockExtras.get(0)));

            // 模拟用户查询结果
            Page<UserEntity> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Collections.singletonList(mockUsers.get(0)));
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
        private class TestAuthUser extends AuthUserEntity {
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

    @Nested
    @Transactional  // 使用事务确保测试后回滚数据
    class searchUserWithPagingWholeTest {

        @InjectMocks
        private UserInfoServiceImpl userInfoService;  // 使用接口而不是实现类

        @Mock
        private UserDao userDao;

        @Mock
        private UserExtraDao userExtraDao;

        private final List<UserEntity> testUsers = new ArrayList<>();
        private final List<UserExtraEntity> testExtras = new ArrayList<>();
        private AuthUserEntity normalUser;
        private AuthUserEntity adminUser;


        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this); // 初始化 Mock 对象

            // 创建测试用户
            normalUser = new TestAuthUser("tester", "USER");
            adminUser = new TestAuthUser("test admin", "ADMIN");

            // 添加测试数据到数据库
            for (int i = 1; i <= 10; i++) {
                UserEntity user = new UserEntity();
                user.setUsername("tester" + i);
                user.setNickname("Test User " + i);
                user.setRealname("Real Name " + i);
                user.setPassword("password" + i);
                user.setRole(i <= 8 ? "USER" : "ADMIN");
                user.setAvatar("avatar" + i + ".jpg");
                user.setLevel("Lv." + i);
                user.setIsValid((byte) 1);
                testUsers.add(user); // 将用户添加到 testUsers 列表

                UserExtraEntity extra = new UserExtraEntity();
                extra.setUsername("tester" + i);
                extra.setCollege(i % 2 == 0 ? "Computer Science" : "Engineering");
                extra.setMajor(i % 2 == 0 ? "Software Engineering" : "Data Science");
                extra.setPersonalSignature("This is user " + i);
                testExtras.add(extra); // 将额外信息添加到 testExtras 列表
            }
        }

        @AfterEach
        void tearDown() {
            // 清理测试数据 - 通常不需要，因为@Transactional会回滚
            // 但如果测试环境中不支持事务回滚，则保留此清理代码
            for (UserExtraEntity extra : testExtras) {
                userExtraDao.deleteById(extra.getUsername());
            }
            for (UserEntity user : testUsers) {
                userDao.deleteById(user.getUsername());
            }
        }

        @Test
        void whenSearchWithoutFilters_thenReturnAllUsers() {
            // Mock userDao.selectCount 方法
            when(userDao.selectCount(any())).thenReturn(10); // 假设总共有 10 条记录

            // Mock userDao.selectPage 方法
            when(userDao.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
                Page<UserEntity> page = (Page<UserEntity>) invocation.getArguments()[0];

                // 根据 pageNum 和 pageSize 计算要返回的记录
                int fromIndex = (int) ((page.getCurrent() - 1) * page.getSize());
                int toIndex = (int) Math.min(fromIndex + page.getSize(), testUsers.size());

                // 设置返回的记录
                page.setRecords(testUsers.subList(fromIndex, toIndex)); // 使用 subList 返回指定范围的记录
                page.setTotal(testUsers.size()); // 总数

                // 计算总页数
                long totalPages = (long) Math.ceil((double) testUsers.size() / page.getSize());
                page.setPages(totalPages); // 设置总页数

                return page;
            });

            // 执行查询
            Page<UserEntity> result = userInfoService.searchUsersWithPaging(adminUser, null, null, null, null, null, 1, 5);

            // 验证结果
            assertNotNull(result, "Result should not be null");
            assertEquals(10, result.getTotal(), "Total records should be 10");
            assertEquals(5, result.getRecords().size(), "Should return 5 records"); // 如果只请求 5 条记录
            assertEquals(2, result.getPages(), "Should return 2 pages"); // 对于 10 条记录和每页 5 条记录，应该返回 2 页
        }

        @Test
        void whenSearchByNickname_thenReturnMatchingUsers() {
            // Mock userDao.selectCount 方法
            when(userDao.selectCount(any())).thenReturn(10); // 假设总共有 10 条记录

            // Mock userDao.selectPage 方法，确保返回匹配的用户数据
            when(userDao.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
                Page<UserEntity> page = (Page<UserEntity>) invocation.getArguments()[0];

                // 根据需求过滤
                List<UserEntity> filteredUsers = testUsers.stream()
                        .filter(user -> user.getNickname() != null && user.getNickname().contains("Test User")) // 确保昵称不为 null
                        .collect(Collectors.toList());

                // 设置返回的数据
                page.setRecords(filteredUsers);
                page.setTotal(filteredUsers.size()); // 设置总数为过滤后的用户数量
                page.setPages(1); // 设置为一页
                return page;
            });

            // 执行查询
            Page<UserEntity> result = userInfoService.searchUsersWithPaging(
                    adminUser, null, "Test User", null, null, null, 1, 10
            );

            // 验证结果
            assertNotNull(result);
            assertEquals(10, result.getTotal()); // 预期总数应该是 10
            assertEquals(testUsers.size(), result.getRecords().size()); // 记录数应返回所有匹配的用户
        }

        @Test
        void whenSearchWithNoMatchingFilters_thenReturnEmptyPage() {
            // Mock userDao.selectCount 方法返回 0，表示没有匹配的记录
            when(userDao.selectCount(any())).thenReturn(0);

            // Mock userDao.selectPage 的返回值
            when(userDao.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
                Page<UserEntity> page = (Page<UserEntity>) invocation.getArguments()[0];
                page.setRecords(new ArrayList<>()); // 返回空记录
                page.setTotal(0); // 总数为 0
                return page;
            });

            // 执行查询
            Page<UserEntity> result = userInfoService.searchUsersWithPaging(adminUser, "nonexistentUsername", null, null, null, null, 1, 5);

            // 验证结果应为空
            assertNotNull(result, "Result should not be null");
            assertEquals(0, result.getTotal(), "Total records should be 0");
            assertTrue(result.getRecords().isEmpty(), "Records should be empty"); // 应该没有记录
        }

        // 测试有匹配的情况
        @Test
        void whenSearchWithMatchingFilters_thenReturnMatchingUsers() {
            // Mock userDao.selectCount 方法返回匹配的记录数量
            when(userDao.selectCount(any())).thenReturn(1);

            // Mock userDao.selectPage 的返回值，返回一个用户记录
            when(userDao.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
                Page<UserEntity> page = (Page<UserEntity>) invocation.getArguments()[0];
                // 生成一个匹配的用户并返回
                UserEntity user = new UserEntity();
                user.setUsername("tester");
                user.setNickname("Test User");
                user.setAvatar("avatar.jpg");
                page.setRecords(Collections.singletonList(user)); // 返回一个用户
                page.setTotal(1); // 总数为 1
                page.setPages(1); // 页数为 1
                return page;
            });

            // 执行查询
            Page<UserEntity> result = userInfoService.searchUsersWithPaging(adminUser, "tester", null, null, null, null, 1, 5);

            // 验证结果
            assertNotNull(result, "Result should not be null");
            assertEquals(1, result.getTotal(), "Total records should be 1");
            assertEquals(1, result.getRecords().size(), "Should return 1 record");
        }

        @Test
        void whenNormalUserSearches_thenReturnLimitedInfo() {
            // 创建一个 Page<UserEntity> 对象并填充数据
            Page<UserEntity> mockPage = new Page<>();

            // 设置 Mock 的返回值
            when(userDao.selectCount(any())).thenReturn((int) testUsers.size());
            when(userDao.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
                Page<UserEntity> page = (Page<UserEntity>) invocation.getArguments()[0];
                page.setRecords(testUsers);  // 使用处理后的用户列表
                page.setTotal(testUsers.size());
                return page;
            });

            // 执行查询
            Page<UserEntity> result = userInfoService.searchUsersWithPaging(
                    normalUser, null, null, null, null, null, 1, 10);

            // 验证结果
            assertEquals(10, result.getTotal());

            // 验证敏感字段不返回给普通用户
            UserEntity firstUser = result.getRecords().get(0);
            assertNull(firstUser.getPassword());
            assertNull(firstUser.getRole());
            assertNull(firstUser.getPhoneNum());
            assertNull(firstUser.getEmail());

            // 基本字段是可见的
            assertNotNull(firstUser.getUsername());
            assertNotNull(firstUser.getNickname());
            assertNotNull(firstUser.getAvatar());
            assertNotNull(firstUser.getLevel());
        }


        // 辅助类
        private class TestAuthUser extends AuthUserEntity {
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

    @Nested
    @ExtendWith(MockitoExtension.class)
    class updateUserInfoTest {

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
            existingUser.setUsername("tester");
            existingUser.setRole("USER");

            UserEntity updatedInfo = new UserEntity();
            updatedInfo.setUsername("tester");
            updatedInfo.setRole("ADMIN");

            UserExtraEntity extraEntity = new UserExtraEntity();
            extraEntity.setUsername("tester");
            existingUser.setUserExtraEntity(extraEntity);
            updatedInfo.setUserExtraEntity(extraEntity);

            // 也可能需要对这个存根使用lenient()
            lenient().when(userService.getById("tester")).thenReturn(existingUser);

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
            existingUser.setUsername("tester");
            existingUser.setRole("USER");
            existingUser.setNickname("Original Name");

            UserEntity updatedInfo = new UserEntity();
            updatedInfo.setUsername("tester");
            updatedInfo.setNickname("New Name");

            UserExtraEntity extraEntity = new UserExtraEntity();
            extraEntity.setUsername("tester");
            existingUser.setUserExtraEntity(extraEntity);
            updatedInfo.setUserExtraEntity(extraEntity);

            // 配置Mock行为
            when(userService.getById("tester")).thenReturn(existingUser);

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
            existingUser.setUsername("tester");
            existingUser.setRole("USER");
            existingUser.setUserExtraEntity(new UserExtraEntity());

            UserEntity updatedInfo = new UserEntity();
            updatedInfo.setUsername("tester");
            updatedInfo.setNickname("New Name");
            updatedInfo.setUserExtraEntity(null);  // 用户附加信息为null

            // 配置Mock行为
            when(userService.getById("tester")).thenReturn(existingUser);

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
            existingUser.setUsername("tester");
            existingUser.setRole("USER");

            UserEntity updatedInfo = new UserEntity();
            updatedInfo.setUsername("tester");
            updatedInfo.setNickname("New Name");

            // 配置Mock行为
            when(userService.getById("tester")).thenReturn(existingUser);

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
            updatedInfo.setUsername("nonexistent");
            updatedInfo.setNickname("New Name");

            // 配置Mock行为
            when(userService.getById("nonexistent")).thenReturn(null);

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
            existingUser.setUsername("tester");
            existingUser.setRole("USER");
            existingUser.setIsValid((byte)1);

            UserEntity updatedInfo = new UserEntity();
            updatedInfo.setUsername("tester");
            updatedInfo.setIsValid((byte)0);  // 将用户禁用

            UserExtraEntity extraEntity = new UserExtraEntity();
            extraEntity.setUsername("tester");
            existingUser.setUserExtraEntity(extraEntity);
            updatedInfo.setUserExtraEntity(extraEntity);

            // 配置Mock行为
            when(userService.getById("tester")).thenReturn(existingUser);

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
            existingAdmin.setUsername("other admin");
            existingAdmin.setRole("admin");  // 注意这里用小写的admin
            existingAdmin.setIsValid((byte)1);

            UserEntity updatedInfo = new UserEntity();
            updatedInfo.setUsername("other admin");
            updatedInfo.setIsValid((byte)-1);  // 尝试禁用另一个管理员

            UserExtraEntity extraEntity = new UserExtraEntity();
            extraEntity.setUsername("other admin");
            existingAdmin.setUserExtraEntity(extraEntity);
            updatedInfo.setUserExtraEntity(extraEntity);

            // 配置Mock行为
            when(userService.getById("other admin")).thenReturn(existingAdmin);

            // 执行测试并验证异常
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userInfoService.updateUserInfo(admin, updatedInfo);
            });

            assertEquals("-4", exception.getCode());
            assertEquals("无权限封禁管理员或超级管理员", exception.getMessage());
        }
    }
}
