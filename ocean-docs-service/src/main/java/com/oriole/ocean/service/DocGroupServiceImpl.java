package com.oriole.ocean.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oriole.ocean.common.po.mysql.GroupEntity;
import com.oriole.ocean.dao.GroupDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DocGroupServiceImpl extends ServiceImpl<GroupDao, GroupEntity> {
    public List<GroupEntity> getDocGroupList(){
        return list();
    }
}
