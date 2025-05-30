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
class getnotacceptedfilelistbyfilestatementtest {

    @Mock
    private FileServiceImpl fileService;

    @Mock
    private FileCheckServiceImpl fileCheckService;

    @Mock
    private UserBehaviorService userBehaviorService;

    @InjectMocks
    private DocInfoController docInfoController;

    private AuthUserEntity adminUser;
    private AuthUserEntity normalUser;
    private List<FileEntity> mockPendingFiles;
    private List<FileEntity> mockApprovedFiles;
    private List<FileEntity> mockRejectedFiles;
    private FileCheckEntity mockFileCheck;

    private List<FileEntity> createMockFiles(int count, String titlePrefix, byte approvalStatus) {
        List<FileEntity> files = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            FileEntity file = new FileEntity();
            file.setFileID(i);
            file.setTitle(titlePrefix + " " + i);
            file.setAbstractContent("这是" + titlePrefix + i + "的摘要内容");
            file.setSize(1024 * i);
            file.setPreviewPictureObjectName("preview_" + i + ".jpg");
            file.setFileType("pdf");
            file.setUploadUsername("testuser");
            file.setUploadDate(new Date());
            file.setRealObjectName("file_" + i + ".pdf");
            file.setPreviewPdfObjectName("preview_pdf_" + i + ".pdf");
            file.setPaymentMethod((byte)0);
            file.setPaymentAmount(0);
            file.setIsAllowAnon((byte)1);
            file.setIsAllowVipfree((byte)1);
            file.setIsAllowComment((byte)1);
            file.setFolderID(1);
            file.setIsApproved(approvalStatus);
            file.setTypeID(1);
            files.add(file);
        }
        return files;
    }

    @BeforeEach
    void setUp() {
        adminUser = mock(AuthUserEntity.class);
        lenient().when(adminUser.isAdmin()).thenReturn(true);
        lenient().when(adminUser.isSuperAdmin()).thenReturn(false);

        normalUser = mock(AuthUserEntity.class);
        lenient().when(normalUser.isAdmin()).thenReturn(false);
        lenient().when(normalUser.isSuperAdmin()).thenReturn(false);

        mockPendingFiles = createMockFiles(3, "待审核文件", (byte)0);
        mockApprovedFiles = createMockFiles(4, "已通过文件", (byte)1);
        mockRejectedFiles = createMockFiles(2, "已拒绝文件", (byte)2);

        mockFileCheck = new FileCheckEntity();
        mockFileCheck.setFileID(1);
        mockFileCheck.setStatus((byte) 1);
        mockFileCheck.setProcessingTime(new Date());
        mockFileCheck.setAgreeReason("内容合规");
        mockFileCheck.setNotice("审核通过");
    }



    @Test
    void whenInvalidApprovedOrStatus_thenReturnErrorMsg() {
        // 执行
        MsgEntity<?> result = docInfoController.getNotAcceptedFileList(adminUser, 5, 1, 10); // 5 是无效的 approvedOrStatus

        // 验证 - 使用更新后的 MsgEntity 结构
        assertEquals("ERROR", result.getState());
        assertEquals("Invalid approvedOrStatus value", result.getMsg()); // 确保 msg 是 String。

        // 确保其他服务方法没有被调用
        verify(fileService, never()).getPendingReviewFiles();
        verify(fileService, never()).getApprovedFiles();
        verify(fileService, never()).getRejectedFiles();
    }

    @Test
    void whenNotAdmin_thenReturnErrorMsg() {
        // 执行
        MsgEntity<PageInfo<FileEntity>> result = docInfoController.getNotAcceptedFileList(normalUser, 0, 1, 10);

        // 验证 - 使用更新后的 MsgEntity 结构
        assertEquals("ERROR", result.getState());
        assertEquals("您没有审核文档的权限", result.getMsg());

        // 确保服务方法没有被调用
        verify(fileService, never()).getPendingReviewFiles();
        verify(fileService, never()).getApprovedFiles();
        verify(fileService, never()).getRejectedFiles();
    }

    @Test
    void whenRequestPendingFiles_thenReturnCorrectData() {
        try (MockedStatic<com.github.pagehelper.PageHelper> pageHelperMock = mockStatic(com.github.pagehelper.PageHelper.class)) {
            // 设置模拟行为
            when(fileService.getPendingReviewFiles()).thenReturn(mockPendingFiles);
            when(fileCheckService.getFileCheckInfo(anyInt())).thenReturn(mockFileCheck);

            // 执行
            MsgEntity<PageInfo<FileEntity>> result = docInfoController.getNotAcceptedFileList(adminUser, 0, 1, 10);

            // 验证 - 使用更新后的MsgEntity结构
            assertEquals("SUCCESS", result.getState());
            assertNotNull(result.getCode());
            assertNotNull(result.getMsg());

            // 验证PageInfo数据
            PageInfo<FileEntity> pageInfo = (PageInfo<FileEntity>) result.getMsg();
            assertNotNull(pageInfo);
            assertEquals(mockPendingFiles.size(), pageInfo.getList().size());

            // 验证服务方法被调用
            verify(fileService).getPendingReviewFiles();
            verify(fileService, never()).getApprovedFiles();
            verify(fileService, never()).getRejectedFiles();

            // 验证文件检查服务为每个文件调用
            verify(fileCheckService, times(mockPendingFiles.size())).getFileCheckInfo(anyInt());

            // 验证返回数据中的文件信息
            for (FileEntity file : pageInfo.getList()) {
                assertNotNull(file.getFileID());
                assertNotNull(file.getTitle());
                assertEquals((byte)0, file.getIsApproved());
            }
        }
    }

    @Test
    void whenRequestApprovedFiles_thenReturnCorrectData() {
        try (MockedStatic<com.github.pagehelper.PageHelper> pageHelperMock = mockStatic(com.github.pagehelper.PageHelper.class)) {
            // 设置模拟行为
            when(fileService.getApprovedFiles()).thenReturn(mockApprovedFiles);
            when(fileCheckService.getFileCheckInfo(anyInt())).thenReturn(mockFileCheck);

            // 执行
            MsgEntity<PageInfo<FileEntity>> result = docInfoController.getNotAcceptedFileList(adminUser, 1, 1, 10);

            // 验证 - 使用更新后的MsgEntity结构
            assertEquals("SUCCESS", result.getState());
            assertNotNull(result.getCode());

            // 验证PageInfo数据
            PageInfo<FileEntity> pageInfo = (PageInfo<FileEntity>) result.getMsg();
            assertNotNull(pageInfo);
            assertEquals(mockApprovedFiles.size(), pageInfo.getList().size());

            // 验证服务方法被调用
            verify(fileService, never()).getPendingReviewFiles();
            verify(fileService).getApprovedFiles();
            verify(fileService, never()).getRejectedFiles();

            // 验证返回的文件有正确的审批状态
            for (FileEntity file : pageInfo.getList()) {
                assertEquals((byte)1, file.getIsApproved());
            }
        }
    }

    @Test
    void whenRequestRejectedFiles_thenReturnCorrectData() {
        try (MockedStatic<com.github.pagehelper.PageHelper> pageHelperMock = mockStatic(com.github.pagehelper.PageHelper.class)) {
            // 修改mockFileCheck为拒绝状态
            mockFileCheck.setStatus((byte) 2);
            mockFileCheck.setRejectReason("内容不合规");
            mockFileCheck.setAgreeReason(null);

            // 设置模拟行为
            when(fileService.getRejectedFiles()).thenReturn(mockRejectedFiles);
            when(fileCheckService.getFileCheckInfo(anyInt())).thenReturn(mockFileCheck);

            // 执行
            MsgEntity<PageInfo<FileEntity>> result = docInfoController.getNotAcceptedFileList(adminUser, 2, 1, 10);

            // 验证 - 使用更新后的MsgEntity结构
            assertEquals("SUCCESS", result.getState());
            assertNotNull(result.getCode());

            // 验证PageInfo数据
            PageInfo<FileEntity> pageInfo = (PageInfo<FileEntity>) result.getMsg();
            assertNotNull(pageInfo);
            assertEquals(mockRejectedFiles.size(), pageInfo.getList().size());

            // 验证服务方法被调用
            verify(fileService, never()).getPendingReviewFiles();
            verify(fileService, never()).getApprovedFiles();
            verify(fileService).getRejectedFiles();

            // 验证返回的文件有正确的审批状态
            for (FileEntity file : pageInfo.getList()) {
                assertEquals((byte)2, file.getIsApproved());
            }
        }
    }


    @Test
    void whenNoFileCheckEntity_thenNoErrorsThrown() {
        try (MockedStatic<com.github.pagehelper.PageHelper> pageHelperMock = mockStatic(com.github.pagehelper.PageHelper.class)) {
            // 设置模拟行为
            when(fileService.getPendingReviewFiles()).thenReturn(mockPendingFiles);
            when(fileCheckService.getFileCheckInfo(anyInt())).thenReturn(null); // 返回null

            // 执行 - 应该不会抛出异常
            MsgEntity<PageInfo<FileEntity>> result = docInfoController.getNotAcceptedFileList(adminUser, 0, 1, 10);

            // 验证 - 使用更新后的MsgEntity结构
            assertEquals("SUCCESS", result.getState());
            PageInfo<FileEntity> pageInfo = (PageInfo<FileEntity>) result.getMsg();
            assertNotNull(pageInfo);

            // 确认文件检查实体为null不会导致问题
            for (FileEntity file : pageInfo.getList()) {
                assertNull(file.getFileCheckEntity());
            }
        }
    }
}