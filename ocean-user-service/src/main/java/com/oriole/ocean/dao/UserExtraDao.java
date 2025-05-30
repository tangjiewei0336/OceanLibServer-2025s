package com.oriole.ocean.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oriole.ocean.common.po.mysql.UserExtraEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserExtraDao extends BaseMapper<UserExtraEntity> {

}