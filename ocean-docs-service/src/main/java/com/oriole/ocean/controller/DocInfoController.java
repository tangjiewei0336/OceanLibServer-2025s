package com.oriole.ocean.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.oriole.ocean.common.auth.AuthUser;
import com.oriole.ocean.common.enumerate.BehaviorType;
import com.oriole.ocean.common.enumerate.MainType;
import com.oriole.ocean.common.po.mongo.UserBehaviorEntity;
import com.oriole.ocean.common.po.mysql.FileCheckEntity;
import com.oriole.ocean.common.po.mysql.FileEntity;
import com.oriole.ocean.common.po.mysql.FileExtraEntity;
import com.oriole.ocean.common.service.UserBehaviorService;
import com.oriole.ocean.common.vo.AuthUserEntity;
import com.oriole.ocean.common.vo.BusinessException;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.service.FileCheckServiceImpl;
import com.oriole.ocean.service.FileServiceImpl;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.oriole.ocean.common.enumerate.ResultCode.*;

@RestController
//@Slf4j
@RequestMapping("/docInfoService")
public class DocInfoController {

    @Autowired
    FileServiceImpl fileService;

    @DubboReference
    UserBehaviorService userBehaviorService;

    @Autowired
    FileCheckServiceImpl fileCheckService;

    @RequestMapping(value = "/getFileList", method = RequestMethod.GET)
    public MsgEntity<PageInfo<FileEntity>> getFileList(@AuthUser AuthUserEntity authUser,
                                                       @RequestParam(required = false) String username,
                                                       @RequestParam Integer pageNum, @RequestParam Integer pageSize,
                                                       @RequestParam Boolean isFolder) {
        username = authUser.getAllowOperationUsername(username);

        PageHelper.startPage(pageNum, pageSize, true);
        PageInfo<FileEntity> fileEntityListPageInfo = new PageInfo<>(fileService.getFileDetailsInfoListByUsername(username, true, isFolder));
        return new MsgEntity<>("SUCCESS", "1", fileEntityListPageInfo);
    }

    @RequestMapping(value = "/getFileListByFolderID", method = RequestMethod.GET)
    public MsgEntity<List<FileEntity>> getFileListByFolderID(@RequestParam String folderID) {
        List<FileEntity> fileEntityList = fileService.getFileDetailsInfoListByFolderID(folderID);
        fileEntityList.forEach(fileEntity -> fileEntity.setRealObjectName(null));
        return new MsgEntity<>("SUCCESS", "1", fileEntityList);
    }

    @RequestMapping(value = "/getFileInfoByFileIDWithAnon", method = RequestMethod.GET)
    public MsgEntity<FileEntity> getFileInfoByFileIDWithAnon(@RequestParam Integer fileID) {
        FileEntity fileEntity = fileService.getFileDetailsInfoByFileID(fileID);
        if (fileEntity.getIsAllowAnon().equals((byte) 0)) {
            throw new BusinessException(DOCS_NOT_ALLOW_ANON_GET);
        }
        if (fileEntity.getIsApproved().equals((byte) 0)) {
            throw new BusinessException(DOCS_NOT_APPROVED);
        }
        fileEntity.setRealObjectName(null);
        return new MsgEntity<>("SUCCESS", "1", fileEntity);
    }

    @RequestMapping(value = "/getFileInfoByFileID", method = RequestMethod.GET)
    public MsgEntity<FileEntity> getFileInfoByFileID(@AuthUser AuthUserEntity authUser, @RequestParam Integer fileID) {
        FileEntity fileEntity = fileService.getFileDetailsInfoByFileID(fileID);

        if (fileEntity.getIsApproved().equals((byte) 0) && !authUser.isUserOwn(fileEntity.getUploadUsername())) {
            throw new BusinessException(DOCS_NOT_APPROVED);
        }
        if (!authUser.isAdmin()) {
            fileEntity.setRealObjectName(null);
        }
        return new MsgEntity<>("SUCCESS", "1", fileEntity);
    }

    @RequestMapping(value = "/changeDocumentInfo", method = RequestMethod.POST)
    public MsgEntity<String> changeDocumentInfo(@AuthUser AuthUserEntity authUser, @RequestParam Integer fileID,
                                                @RequestParam(required = false) String title,
                                                @RequestParam(required = false) String abstractContent,
                                                @RequestParam(required = false) Byte paymentMethod,
                                                @RequestParam Integer paymentAmount,
                                                @RequestParam(required = false) Byte isAllowAnon,
                                                @RequestParam(required = false) Byte isAllowVipfree,
                                                @RequestParam(required = false) Byte isAllowComment,
                                                @RequestParam(required = false) Integer folderID,
                                                @RequestParam(required = false) String copyrightNotice,
                                                @RequestParam(required = false) String originalAuthor,

                                                @RequestParam(required = false) Byte isProCert,
                                                @RequestParam(required = false) Byte isOfficial,
                                                @RequestParam(required = false) Byte isOriginal,
                                                @RequestParam(required = false) Byte isVipIncome,
                                                @RequestParam(required = false) String uploadDate

    ) {
        FileEntity fileEntity = fileService.getFileBaseInfoByFileID(fileID);

        if (!authUser.isUserOwn(fileEntity.getUploadUsername())) {
            throw new BusinessException(UNAUTHORIZED_OPERATION);
        }

        FileEntity newFileEntity = new FileEntity(fileID, title, abstractContent,
                paymentMethod, paymentAmount,
                isAllowAnon, isAllowVipfree, isAllowComment,
                folderID, uploadDate);

        FileExtraEntity newFileExtraEntity = new FileExtraEntity(newFileEntity.getFileID(), isOriginal, originalAuthor, copyrightNotice,
                isProCert, isOfficial, isVipIncome);
        newFileEntity.setFileExtraEntity(newFileExtraEntity);

        if (!authUser.isAdmin()) {
            // 重新审核
            newFileEntity.setIsApproved((byte) 0);
        }
        // 写数据库
        fileService.saveOrUpdateFileInfo(newFileEntity);
        return new MsgEntity<>(SUCCESS);
    }

    @RequestMapping(value = "/getNotAcceptedFileList", method = RequestMethod.GET)
    public MsgEntity<PageInfo<FileEntity>> getNotAcceptedFileList(@AuthUser AuthUserEntity authUser,
                                                                  @RequestParam(required = false) String username,
                                                                  @RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        username = authUser.getAllowOperationUsername(username);

        PageHelper.startPage(pageNum, pageSize, true);
        List<FileEntity> fileEntityList = fileService.getFileDetailsInfoListByUsername(username, false, false);
        fileEntityList.forEach(fileEntity -> {
            if (!fileEntity.getIsApproved().equals((byte) 0)) {
                FileCheckEntity fileCheckEntity = fileCheckService.getFileCheckInfo(fileEntity.getFileID());
                fileEntity.setFileCheckEntity(fileCheckEntity);
            }
        });
        PageInfo<FileEntity> fileEntityListPageInfo = new PageInfo<>(fileEntityList);
        return new MsgEntity<>("SUCCESS", "1", fileEntityListPageInfo);
    }

    @RequestMapping(value = "/getNotAcceptedFileListByFileStatement", method = RequestMethod.GET)
    public MsgEntity<PageInfo<FileEntity>> getNotAcceptedFileList(@AuthUser AuthUserEntity authUser,
                                                                  @RequestParam Integer approvedOrStatus,
                                                                  @RequestParam Integer pageNum,
                                                                  @RequestParam Integer pageSize) {
        if (!authUser.isAdmin() && !authUser.isSuperAdmin()) {
            return new MsgEntity<>("ERROR", "您没有审核文档的权限", null);
        }

        // 检查 approvedOrStatus 的有效性
        if (approvedOrStatus == null || (approvedOrStatus < 0 || approvedOrStatus > 2)) {
            return new MsgEntity<>("ERROR", "Invalid approvedOrStatus value", null);
        }

        List<FileEntity> fileEntityList = new ArrayList<>(); // 初始化为一个空列表，以避免NullPointerException

        PageHelper.startPage(pageNum, pageSize, true);

        // 根据状态调用对应的方法
        switch (approvedOrStatus) {
            case 0:
                fileEntityList = fileService.getPendingReviewFiles();
                break;
            case 1:
                fileEntityList = fileService.getApprovedFiles();
                break;
            case 2:
                fileEntityList = fileService.getRejectedFiles();
                break;
            default:
                return new MsgEntity<>("ERROR", "400", new PageInfo<>()); // 返回一个空的 PageInfo
        }

        // 确保 fileEntityList 不为 null
        if (fileEntityList == null) {
            fileEntityList = new ArrayList<>();
        }

        // 补充文件检查信息
        for (FileEntity fileEntity : fileEntityList) {
            if (fileEntity.getFileID() != null) {
                FileCheckEntity fileCheckEntity = fileCheckService.getFileCheckInfo(fileEntity.getFileID());
                if (fileCheckEntity != null) {
                    fileEntity.setFileCheckEntity(fileCheckEntity);
                }
            }
        }

        // 使用 PageInfo 包装返回的文件列表
        PageInfo<FileEntity> fileEntityListPageInfo = new PageInfo<>(fileEntityList);

        // 返回 MsgEntity<PageInfo<FileEntity>>
        return new MsgEntity<>("SUCCESS", "200", fileEntityListPageInfo); // 确保返回类型一致
    }


    @RequestMapping(value = "/getRecentlyReadList", method = RequestMethod.GET)
    public MsgEntity<List<FileEntity>> getRecentlyReadList(@AuthUser AuthUserEntity authUser,
                                                           @RequestParam(required = false) String username) {
        username = authUser.getAllowOperationUsername(username);

        UserBehaviorEntity UserBehaviorEntityQuery = new UserBehaviorEntity(null, MainType.DOCUMENT, username, BehaviorType.DO_READ);
        List<UserBehaviorEntity> userBehaviorEntities = userBehaviorService.findAllBehaviorRecords(UserBehaviorEntityQuery, 7);

        List<Integer> fileIDs = userBehaviorEntities.stream().map(UserBehaviorEntity::getBindID).distinct().collect(Collectors.toList());

        List<FileEntity> fileEntities = fileService.getFileDetailsInfoListByFileIDs(fileIDs);

        // 按照 fileIDs 的顺序排列 fileEntities 的顺序
        List<FileEntity> fileEntitiesSequential = fileEntities.stream().sorted(
                Comparator.comparingInt(o -> fileIDs.indexOf((o.getFileID())))
        ).distinct().collect(Collectors.toList());

        return new MsgEntity<>("SUCCESS", "1", fileEntitiesSequential);
    }

    @RequestMapping(value = "/getDownloadList", method = RequestMethod.GET)
    public MsgEntity<List<FileEntity>> getDownloadList(@AuthUser AuthUserEntity authUser,
                                                       @RequestParam(required = false) String username) {

        username = authUser.getAllowOperationUsername(username);

        UserBehaviorEntity UserBehaviorEntityQuery = new UserBehaviorEntity(null, MainType.DOCUMENT, username, BehaviorType.DO_DOWNLOAD);
        List<UserBehaviorEntity> userBehaviorEntities = userBehaviorService.findAllBehaviorRecords(UserBehaviorEntityQuery);

        List<Integer> fileIDs = userBehaviorEntities.stream().map(UserBehaviorEntity::getBindID).distinct().collect(Collectors.toList());

        List<FileEntity> fileEntities = new ArrayList<>();
        if (fileIDs.size() > 0) {
            fileEntities = fileService.getFileDetailsInfoListByFileIDs(fileIDs);
        }
        return new MsgEntity<>("SUCCESS", "1", fileEntities);
    }

    @RequestMapping(value = "/submitFolderInfo", method = RequestMethod.GET)
    public MsgEntity<String> submitFolderInfo(@AuthUser String username, @RequestParam String title, @RequestParam String abstractContent,
                                              @RequestParam Integer folderID) {
        fileService.saveOrUpdateFileInfo(new FileEntity(title, abstractContent, username, folderID));
        return new MsgEntity<>(SUCCESS);
    }
}
