package com.oriole.ocean.service.base;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oriole.ocean.dao.UserExtraDao;
import com.oriole.ocean.common.po.mysql.UserExtraEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserExtraInfoServiceImpl extends ServiceImpl<UserExtraDao, UserExtraEntity> {
}
