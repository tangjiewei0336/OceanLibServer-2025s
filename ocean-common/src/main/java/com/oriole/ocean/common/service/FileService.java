package com.oriole.ocean.common.service;

import com.oriole.ocean.common.po.mysql.FileEntity;

public interface FileService {
    FileEntity getFileBaseInfoByFileID(Integer fileID);
    void saveOrUpdateFileInfo(FileEntity fileEntity);
}
