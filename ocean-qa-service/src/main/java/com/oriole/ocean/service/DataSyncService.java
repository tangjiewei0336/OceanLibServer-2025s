package com.oriole.ocean.service;

/**
 * 数据同步服务接口
 */
public interface DataSyncService {
    /**
     * 从 MongoDB 同步数据到 Elasticsearch
     */
    void syncData();
}
