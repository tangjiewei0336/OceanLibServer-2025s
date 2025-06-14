package com.oriole.ocean.controller;

import com.github.pagehelper.PageInfo;
import com.oriole.ocean.common.vo.AuthUserEntity;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.common.po.mysql.FileEntity;
import com.oriole.ocean.common.po.mysql.FileCheckEntity;
import com.oriole.ocean.service.FileServiceImpl;
import com.oriole.ocean.service.FileCheckServiceImpl;
import com.oriole.ocean.common.service.UserBehaviorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocInfoControllerUnitTest {

    @Mock
    private FileServiceImpl fileService;

    @Mock
    private FileCheckServiceImpl fileCheckService;

    @Mock
    private UserBehaviorService userBehaviorService;

    @InjectMocks
    private DocInfoController docInfoController;

    @Test
    void whenInvalidApprovedOrStatus_thenReturnErrorMsg() {
        // 创建管理员用户 - 只mock需要的方法
        AuthUserEntity adminUser = mock(AuthUserEntity.class);
        when(adminUser.isAdmin()).thenReturn(true);
        // 注意：不设置isSuperAdmin()，因为这个测试用不到

        // 执行
        MsgEntity<PageInfo<FileEntity>> result = docInfoController.getNotAcceptedFileList(
                adminUser, 5, 1, 10
        );

        // 验证
        assertNotNull(result);
        assertEquals("ERROR", result.getState());
        // 根据你的MsgEntity结构调整这里
        assertEquals("Invalid approvedOrStatus value", result.getCode()); // 如果错误信息在code中

        // 验证没有调用服务方法
        verifyNoInteractions(fileService, fileCheckService, userBehaviorService);
    }

    @Test
    void whenNotAdmin_thenReturnErrorMsg() {
        // 创建普通用户 - 只mock需要的方法
        AuthUserEntity normalUser = mock(AuthUserEntity.class);
        when(normalUser.isAdmin()).thenReturn(false);
        when(normalUser.isSuperAdmin()).thenReturn(false);

        // 执行
        MsgEntity<PageInfo<FileEntity>> result = docInfoController.getNotAcceptedFileList(
                normalUser, 0, 1, 10
        );

        // 验证
        assertNotNull(result);
        assertEquals("ERROR", result.getState());
        // 根据你的MsgEntity结构调整这里
        assertEquals("您没有审核文档的权限", result.getCode()); // 如果错误信息在code中

        // 验证没有调用服务方法
        verifyNoInteractions(fileService, fileCheckService, userBehaviorService);
    }

    @Test
    void whenRequestPendingFiles_thenReturnCorrectData() {
        // 创建管理员用户
        AuthUserEntity adminUser = mock(AuthUserEntity.class);
        when(adminUser.isAdmin()).thenReturn(true);

        // 准备测试数据
        List<FileEntity> mockFiles = createMockFiles(3, "待审核文件", (byte)0);
        FileCheckEntity mockFileCheck = new FileCheckEntity();
        mockFileCheck.setFileID(1);
        mockFileCheck.setStatus((byte) 1);

        // 设置Mock行为
        when(fileService.getPendingReviewFiles()).thenReturn(mockFiles);
        when(fileCheckService.getFileCheckInfo(anyInt())).thenReturn(mockFileCheck);

        // 执行
        MsgEntity<PageInfo<FileEntity>> result = docInfoController.getNotAcceptedFileList(
                adminUser, 0, 1, 10
        );

        // 验证
        assertNotNull(result);
        assertEquals("SUCCESS", result.getState());
        assertEquals("200", result.getCode());

        // 根据你的MsgEntity结构调整 - PageInfo在msg字段中
        PageInfo<FileEntity> pageInfo = result.getMsg();
        assertNotNull(pageInfo);
        assertEquals(mockFiles.size(), pageInfo.getList().size());

        // 验证服务方法被调用
        verify(fileService).getPendingReviewFiles();
        verify(fileCheckService, times(mockFiles.size())).getFileCheckInfo(anyInt());
    }

    // 辅助方法
    private List<FileEntity> createMockFiles(int count, String titlePrefix, byte approvalStatus) {
        List<FileEntity> files = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            FileEntity file = new FileEntity();
            file.setFileID(i);
            file.setTitle(titlePrefix + " " + i);
            file.setIsApproved(approvalStatus);
            files.add(file);
        }
        return files;
    }
}