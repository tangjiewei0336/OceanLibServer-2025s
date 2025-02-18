package com.oriole.ocean.service.base;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oriole.ocean.dao.UserCertificationDao;
import com.oriole.ocean.common.po.mysql.UserCertificationEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserCertificationServiceImpl extends ServiceImpl<UserCertificationDao, UserCertificationEntity> {
}
