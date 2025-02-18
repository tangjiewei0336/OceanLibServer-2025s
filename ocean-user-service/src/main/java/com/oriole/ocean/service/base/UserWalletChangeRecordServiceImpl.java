package com.oriole.ocean.service.base;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.oriole.ocean.dao.WalletChangeRecordDao;
import com.oriole.ocean.common.po.mysql.WalletChangeRecordEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserWalletChangeRecordServiceImpl extends ServiceImpl<WalletChangeRecordDao, WalletChangeRecordEntity> {

    public PageInfo<WalletChangeRecordEntity> getWalletChangeRecord(String username, Integer pageNum, Integer pageSize) {
        QueryWrapper<WalletChangeRecordEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);

        PageHelper.startPage(pageNum, pageSize, true);

        List<WalletChangeRecordEntity> walletChangeRecordEntityList = list(queryWrapper);

        return new PageInfo<>(walletChangeRecordEntityList);
    }

}
