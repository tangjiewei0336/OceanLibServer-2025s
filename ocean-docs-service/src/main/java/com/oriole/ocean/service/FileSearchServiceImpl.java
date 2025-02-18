package com.oriole.ocean.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oriole.ocean.common.service.FileSearchService;
import com.oriole.ocean.dao.FileSearchDao;
import com.oriole.ocean.common.po.mysql.FileSearchEntity;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@DubboService
@Transactional
public class FileSearchServiceImpl extends ServiceImpl<FileSearchDao, FileSearchEntity> implements FileSearchService {
    // 新增文档检索信息（ES自动同步，无需手动操作）
    public void saveOrUpdateFileSearchInfo(FileSearchEntity fileSearchEntity){
        saveOrUpdate(fileSearchEntity);
    }
}
