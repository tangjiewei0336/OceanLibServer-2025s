package com.oriole.ocean.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oriole.ocean.common.po.mysql.UserEntity;
import com.oriole.ocean.common.po.mysql.UserExtraEntity;
import com.oriole.ocean.common.vo.AuthUserEntity;
import com.oriole.ocean.dao.UserDao;
import com.oriole.ocean.dao.UserExtraDao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional  // 使用事务确保测试后回滚数据
public class searchUserWithPagingWholeTest {

    @Mock  // 使用@Autowired代替@Mock来获取真实的bean
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
        // 创建测试用户
        normalUser = new TestAuthUser("testuser", "USER");
        adminUser = new TestAuthUser("testadmin", "ADMIN");

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

            userDao.insert(user);
            testUsers.add(user);

            UserExtraEntity extra = new UserExtraEntity();
            extra.setUsername("tester" + i);
            extra.setCollege(i % 2 == 0 ? "Computer Science" : "Engineering");
            extra.setMajor(i % 2 == 0 ? "Software Engineering" : "Data Science");
            extra.setPersonalSignature("This is user " + i);

            userExtraDao.insert(extra);
            testExtras.add(extra);
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
        // 执行查询
        Page<UserEntity> result = userInfoService.searchUsersWithPaging(
                adminUser, null, null, null, null, null, 1, 5);

        // 验证结果
        assertEquals(10, result.getTotal());
        assertEquals(5, result.getRecords().size());
        assertEquals(2, result.getPages());
    }

    @Test
    void whenSearchByNickname_thenReturnMatchingUsers() {
        // 执行查询
        Page<UserEntity> result = userInfoService.searchUsersWithPaging(
                adminUser, null, "Test User 1", null, null, null, 1, 10);

        // 验证结果 - 应该匹配 "Test User 1" 和 "Test User 10"
        assertEquals(2, result.getTotal());
    }

    @Test
    void whenSearchByCollege_thenReturnMatchingUsers() {
        // 执行查询
        Page<UserEntity> result = userInfoService.searchUsersWithPaging(
                adminUser, null, null, null, "Computer Science", null, 1, 10);

        // 验证结果 - 应该匹配所有偶数用户
        assertEquals(5, result.getTotal());

        // 验证所有返回的用户都属于 Computer Science 学院
        for (UserEntity user : result.getRecords()) {
            assertEquals("Computer Science", user.getUserExtraEntity().getCollege());
        }
    }

    @Test
    void whenNormalUserSearches_thenReturnLimitedInfo() {
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

        // 但基本字段是可见的
        assertNotNull(firstUser.getUsername());
        assertNotNull(firstUser.getNickname());
        assertNotNull(firstUser.getAvatar());
        assertNotNull(firstUser.getLevel());
        assertNotNull(firstUser.getUserExtraEntity());
    }

    @Test
    void whenAdminSearches_thenReturnFullInfo() {
        // 执行查询
        Page<UserEntity> result = userInfoService.searchUsersWithPaging(
                adminUser, null, null, null, null, null, 1, 10);

        // 验证结果
        assertEquals(10, result.getTotal());

        // 验证管理员可以看到敏感字段
        UserEntity firstUser = result.getRecords().get(0);
        assertNotNull(firstUser.getPassword());
        assertNotNull(firstUser.getRole());
        assertNotNull(firstUser.getIsValid());
    }

    @Test
    void whenSearchWithMultipleFilters_thenCombineConditions() {
        // 执行查询 - 组合多个条件
        Page<UserEntity> result = userInfoService.searchUsersWithPaging(
                adminUser, null, "Test", null, "Engineering", "Data", 1, 10);

        // 验证结果 - 应该匹配所有奇数用户中专业包含"Data"的用户
        assertTrue(result.getTotal() > 0);

        for (UserEntity user : result.getRecords()) {
            assertEquals("Engineering", user.getUserExtraEntity().getCollege());
            assertTrue(user.getUserExtraEntity().getMajor().contains("Data"));
        }
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