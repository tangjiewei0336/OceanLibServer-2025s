package com.oriole.ocean.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oriole.ocean.dao.TypeDao;
import com.oriole.ocean.common.po.mysql.FileTypeEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DocTypeServiceImpl extends ServiceImpl<TypeDao, FileTypeEntity> {

    public List<FileTypeEntity> getTypeListByTypeString(String typeString){
        String[] typeIDs=typeString.split("\\,");
        QueryWrapper<FileTypeEntity> queryWrapper = new QueryWrapper<>();
        for(String typeID:typeIDs) {
            queryWrapper.eq("type_ID", typeID).or();
        }
        return list(queryWrapper);
    }
}
