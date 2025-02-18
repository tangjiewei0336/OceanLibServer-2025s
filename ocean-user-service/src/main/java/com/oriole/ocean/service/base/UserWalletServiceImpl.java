package com.oriole.ocean.service.base;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oriole.ocean.common.enumerate.VipOperationState;
import com.oriole.ocean.common.enumerate.VipOperationType;
import com.oriole.ocean.common.service.UserWalletService;
import com.oriole.ocean.dao.WalletDao;
import com.oriole.ocean.common.po.mysql.WalletChangeRecordEntity;
import com.oriole.ocean.common.po.mysql.WalletEntity;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

@Service
@DubboService
@Transactional
public class UserWalletServiceImpl extends ServiceImpl<WalletDao, WalletEntity> implements UserWalletService {

    @Autowired
    UserWalletChangeRecordServiceImpl walletChangeRecordService;

    public boolean changeWalletCoin(String username,Integer changingNum,String reason,Integer fileID){
        WalletEntity walletEntity=getById(username);
        Integer changedNum=walletEntity.getCoin()+changingNum;
        if(changedNum>0){
            walletEntity.setCoin(changedNum);
            saveOrUpdate(walletEntity);
            walletChangeRecordService.save(new WalletChangeRecordEntity(username,"Coin",changingNum,reason,fileID));
            return true;
        }else {
            return false;
        }
    }

    public boolean changeWalletTicket(String username,Integer changingNum,String reason,Integer fileID){
        WalletEntity walletEntity=getById(username);
        Integer changedNum=walletEntity.getTicket()+changingNum;
        if(changedNum>0){
            walletEntity.setTicket(changedNum);
            saveOrUpdate(walletEntity);
            walletChangeRecordService.save(new WalletChangeRecordEntity(username,"Ticket",changingNum,reason,fileID));
            return true;
        }else {
            return false;
        }
    }

    public VipOperationState isVipAndIsCanDo(String username, VipOperationType vipOperationType) {
        if (getById(username).getIsVip().equals((byte) 1)){
            switch (vipOperationType){
                case DOWNLOAD_DOCS:
                    return VipOperationState.DO;
                default:
                    return VipOperationState.NOT_SUPPORT;
            }
        }else {
            return VipOperationState.NOT_VIP;
        }
    }
}
