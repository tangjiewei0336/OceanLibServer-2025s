package com.oriole.ocean.service.base;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.oriole.ocean.common.po.mysql.UserEntity;
import com.oriole.ocean.common.vo.AuthUserEntity;
import com.oriole.ocean.common.vo.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for the UserBaseInfoServiceImpl class.
 * We use @Spy to create a partial mock, allowing us to test the banUser method
 * while mocking the database-dependent methods (getById, updateById) it calls.
 */
@ExtendWith(MockitoExtension.class)
class searchUsersWithPagingTest {

    @Spy // Using @Spy to test one method while mocking others in the same class
    @InjectMocks
    private UserBaseInfoServiceImpl userBaseService;

    // Actors (the ones performing the action)
    private AuthUserEntity adminUser;
    private AuthUserEntity superAdminUser;
    private AuthUserEntity regularUser;
    private AuthUserEntity normalUser;

    @BeforeEach
    void setUp() {
        adminUser = mock(AuthUserEntity.class);
        lenient().when(adminUser.isAdmin()).thenReturn(true);
        lenient().when(adminUser.isSuperAdmin()).thenReturn(false);
        lenient().when(adminUser.getRole()).thenReturn("ADMIN");

        // 添加 superAdminUser 的初始化
        superAdminUser = mock(AuthUserEntity.class);
        lenient().when(superAdminUser.isAdmin()).thenReturn(true);
        lenient().when(superAdminUser.isSuperAdmin()).thenReturn(true);
        lenient().when(superAdminUser.getRole()).thenReturn("SUPERADMIN");

        normalUser = mock(AuthUserEntity.class);
        lenient().when(normalUser.isAdmin()).thenReturn(false);
        lenient().when(normalUser.isSuperAdmin()).thenReturn(false);
        lenient().when(normalUser.getRole()).thenReturn("USER");
    }

    @Nested
    @DisplayName("Successful Ban Scenarios")
    class SuccessCases {

        @Test
        @DisplayName("SUPERADMIN should successfully ban an ADMIN")
        void whenSuperAdminBansAdmin_thenSuccess() {
            // Arrange: The user to be banned is an ADMIN
            UserEntity targetAdmin = new UserEntity();
            targetAdmin.setUsername("targetAdmin");
            targetAdmin.setRole("ADMIN");
            targetAdmin.setIsValid((byte) 1);

            // Mock the methods that interact with the database
            doReturn(targetAdmin).when(userBaseService).getById("targetAdmin");
            doReturn(true).when(userBaseService).updateById(any(UserEntity.class));

            // Act
            UserEntity result = userBaseService.banUser(superAdminUser, "targetAdmin");

            // Assert
            assertNotNull(result);
            assertEquals((byte) -1, result.getIsValid(), "User's isValid flag should be set to -1");

            // Verify that updateById was called with the correctly modified entity
            ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
            verify(userBaseService).updateById(userCaptor.capture());
            assertEquals((byte) -1, userCaptor.getValue().getIsValid());
        }

        @Test
        @DisplayName("ADMIN should successfully ban a USER")
        void whenAdminBansUser_thenSuccess() {
            // Arrange: The user to be banned is a regular USER
            UserEntity targetUser = new UserEntity();
            targetUser.setUsername("targetUser");
            targetUser.setRole("USER");
            targetUser.setIsValid((byte) 1);

            doReturn(targetUser).when(userBaseService).getById("targetUser");
            doReturn(true).when(userBaseService).updateById(any(UserEntity.class));

            // Act
            UserEntity result = userBaseService.banUser(adminUser, "targetUser");

            // Assert
            assertNotNull(result);
            assertEquals((byte) -1, result.getIsValid());
            verify(userBaseService).updateById(any(UserEntity.class));
        }
    }

    @Nested
    @DisplayName("Permission Denied Scenarios")
    class PermissionDeniedCases {

        @Test
        @DisplayName("Should throw exception when ADMIN tries to ban a SUPERADMIN")
        void whenAdminTriesToBanSuperAdmin_thenThrowException() {
            // Arrange
            UserEntity targetSuperAdmin = new UserEntity();
            targetSuperAdmin.setRole("SUPERADMIN");
            doReturn(targetSuperAdmin).when(userBaseService).getById("superAdmin");

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userBaseService.banUser(adminUser, "superAdmin");
            });

            assertEquals("-2", exception.getCode());
            assertEquals("Permission denied: Cannot ban SUPERADMIN", exception.getMessage());
            verify(userBaseService, never()).updateById(any());
        }

        @Test
        @DisplayName("Should throw exception when ADMIN tries to ban another ADMIN")
        void whenAdminTriesToBanAnotherAdmin_thenThrowException() {
            // Arrange
            UserEntity targetAdmin = new UserEntity();
            targetAdmin.setRole("ADMIN");
            doReturn(targetAdmin).when(userBaseService).getById("anotherAdmin");

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userBaseService.banUser(adminUser, "anotherAdmin");
            });

            assertEquals("-2", exception.getCode());
            assertEquals("Permission denied: Administrators cannot ban each other", exception.getMessage());
            verify(userBaseService, never()).updateById(any());
        }

        @Test
        @DisplayName("Should throw exception when a non-admin USER tries to ban someone")
        void whenUserTriesToBan_thenThrowException() {
            // Arrange
            UserEntity targetUser = new UserEntity();
            targetUser.setRole("USER");
            doReturn(targetUser).when(userBaseService).getById("targetUser");

            // Act & Assert - 使用 normalUser 而不是 regularUser
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userBaseService.banUser(normalUser, "targetUser");
            });

            assertEquals("-2", exception.getCode());
            assertEquals("Permission denied: Only administrators can ban users", exception.getMessage());
            verify(userBaseService, never()).updateById(any());
        }
    }

    @Nested
    @DisplayName("Edge Case Scenarios")
    class EdgeCases {
        @Test
        @DisplayName("Should throw exception when trying to ban a user that does not exist")
        void whenBanningNonExistentUser_thenThrowException() {
            // Arrange: Mock getById to return null, simulating a non-existent user
            doReturn(null).when(userBaseService).getById("nonexistent");

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userBaseService.banUser(adminUser, "nonexistent");
            });

            assertEquals("-1", exception.getCode());
            assertEquals("User not found", exception.getMessage());
            verify(userBaseService, never()).updateById(any());
        }
    }
}