package com.oriole.ocean.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oriole.ocean.dao.FileUploadTempDao;
import com.oriole.ocean.common.po.mysql.FileUploadTempEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FileUploadTempService extends ServiceImpl<FileUploadTempDao, FileUploadTempEntity> {
    //新增或变更临时文档信息
    public void saveOrUpdateUploadTempFileInfo(FileUploadTempEntity fileUploadTempEntity) {
        saveOrUpdate(fileUploadTempEntity);
    }

    //文档上传临时文件信息查询
    public FileUploadTempEntity getUploadTempFileInfo(Integer uploadID, String username) {
        QueryWrapper<FileUploadTempEntity> queryWrapper = new QueryWrapper<>();
        if (username != null) {
            queryWrapper.eq("upload_username", username);
        }
        queryWrapper.eq("upload_id", uploadID);
        return getOne(queryWrapper);
    }

    //文档上传临时文件信息批量查询
    public List<FileUploadTempEntity> getAllUploadTempFileInfoByUsername(String username) {
        QueryWrapper<FileUploadTempEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("upload_username", username);
        return list(queryWrapper);
    }
}
