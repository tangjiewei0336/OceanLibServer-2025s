package com.oriole.ocean.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oriole.ocean.common.po.mysql.FileEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface FileDao extends BaseMapper<FileEntity> {
    List<FileEntity> getFileListByTypeIDAndTagIDAndIndexString(Integer typeID,String[] tagIDs,String indexString);
    List<FileEntity> getFileListByUsername(String username,Boolean isApproved, Boolean isFolder);
    List<FileEntity> getFileListByFolderID(String folderID);

    FileEntity getFileDetailInfoById(@Param("fileID") Integer fileID);
    List<FileEntity> getFileDetailInfosByIds(@Param("fileIDs") List<Integer> fileIDs);
}