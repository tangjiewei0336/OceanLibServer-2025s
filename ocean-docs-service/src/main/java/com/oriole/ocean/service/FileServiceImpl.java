package com.oriole.ocean.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oriole.ocean.common.po.mysql.FileExtraEntity;
import com.oriole.ocean.common.service.FileService;
import com.oriole.ocean.dao.FileDao;
import com.oriole.ocean.common.po.mysql.FileEntity;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@DubboService
@Transactional
public class FileServiceImpl extends ServiceImpl<FileDao, FileEntity> implements FileService {
    @Resource
    FileDao fileDao;

    @Autowired
    FileExtraServiceImpl fileExtraService;

    //文章基础信息查询
    public FileEntity getFileBaseInfoByFileID(Integer fileID) {
        return getById(fileID);
    }

    //文章详细信息查询
    public FileEntity getFileDetailsInfoByFileID(Integer fileIDs) {
        return fileDao.getFileDetailInfoById(fileIDs);
    }

    //文章详细信息查询（通过用户名查询）（可区分是否已被审核）
    public List<FileEntity> getFileDetailsInfoListByUsername(String username, Boolean isApproved, Boolean isFolder) {
        return fileDao.getFileListByUsername(username, isApproved, isFolder);
    }

    //文章详细信息查询（通过分类ID、标签ID和索引字符串查询）
    public List<FileEntity> getFileDetailsInfoListByTypeIDAndTagStringAndIndexString(Integer typeID, String tagString, String indexString) {
        String[] tagIDs = new String[0];
        if (!tagString.isEmpty() && !tagString.equals("null")) {
            tagIDs = tagString.split("\\,");
        }
        return fileDao.getFileListByTypeIDAndTagIDAndIndexString(typeID, tagIDs, indexString + "%");
    }

    //文章详细信息查询（通过FolderID查询）
    public List<FileEntity> getFileDetailsInfoListByFolderID(String folderID) {
        return fileDao.getFileListByFolderID(folderID);
    }

    //文章详细信息批量查询（通过FileID列表查询）
    public List<FileEntity> getFileDetailsInfoListByFileIDs(List<Integer> fileIDs) {
        return fileDao.getFileDetailInfosByIds(fileIDs);
    }

    //保存或更新文章信息
    public void saveOrUpdateFileInfo(FileEntity fileEntity) {
        saveOrUpdateFileInfo(fileEntity);
        FileExtraEntity fileExtraEntity = fileEntity.getFileExtraEntity();
        if (fileExtraEntity != null) {
            fileExtraEntity.setFileID(fileEntity.getFileID());
            fileExtraService.saveOrUpdateFileExtraInfo(fileExtraEntity);
        }
    }
}