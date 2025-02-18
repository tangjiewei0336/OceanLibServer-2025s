package com.oriole.ocean.common.service;

import com.oriole.ocean.common.po.mysql.FileSearchEntity;

public interface FileSearchService {
    void saveOrUpdateFileSearchInfo(FileSearchEntity fileSearchEntity);
}
