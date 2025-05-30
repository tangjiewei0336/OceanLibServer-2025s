package com.oriole.ocean.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oriole.ocean.common.po.mysql.FileEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface FileDao extends BaseMapper<FileEntity> {
    List<FileEntity> getFileListByTypeIDAndTagIDAndIndexString(Integer typeID,String[] tagIDs,String indexString);
    List<FileEntity> getFileListByUsername(String username,Boolean isApproved, Boolean isFolder);
    List<FileEntity> getFileListByFolderID(String folderID);
    List<FileEntity> getFileListByIsApproved(Byte IsApproved);

    FileEntity getFileDetailInfoById(@Param("fileID") Integer fileID);
    List<FileEntity> getFileDetailInfosByIds(@Param("fileIDs") List<Integer> fileIDs);

    /**
     * 获取待审核的文件列表
     * (is_approved = 0 且 没有对应的file_check记录 或 status = 0且未处理)
     */
    List<FileEntity> getPendingReviewFiles();

    /**
     * 获取已审核通过的文件列表
     * (is_approved = 1)
     */
    List<FileEntity> getApprovedFiles();

    /**
     * 获取已拒绝的文件列表
     * (is_approved = 0 且 status = 1且已处理)
     */
    List<FileEntity> getRejectedFiles();
}