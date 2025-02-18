package com.oriole.ocean.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oriole.ocean.dao.FileCheckDao;
import com.oriole.ocean.common.po.mysql.FileCheckEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FileCheckServiceImpl extends ServiceImpl<FileCheckDao, FileCheckEntity> {
    // 新增或变更文档审核信息
    public void saveOrUpdateFileCheckInfo(FileCheckEntity fileCheckEntity){
        saveOrUpdate(fileCheckEntity);
    }
    // 获取文档审核信息
    public FileCheckEntity getFileCheckInfo(Integer fileID){
        return getById(fileID);
    }
}
