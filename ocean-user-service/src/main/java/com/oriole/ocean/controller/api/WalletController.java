package com.oriole.ocean.controller.api;

import com.oriole.ocean.common.enumerate.MainType;
import com.oriole.ocean.common.enumerate.VipOperationState;
import com.oriole.ocean.common.enumerate.VipOperationType;
import com.oriole.ocean.common.enumerate.WalletMoneyType;
import com.oriole.ocean.service.base.UserWalletServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/walletAPI")
public class WalletController {

    @Autowired
    UserWalletServiceImpl walletService;

    @PostMapping(value = "/changeWallet")
    public boolean changeWallet(@RequestParam String username,
                                @RequestParam Integer changingNum,
                                @RequestParam String reason,
                                @RequestParam Integer itemID,
                                @RequestParam WalletMoneyType walletMoneyType,
                                @RequestParam MainType mainType) {
        //TODO: 未改造为通用的钱包，这里是文件付费，但预留了mainType接口
        switch (walletMoneyType){
            case COIN:
                return walletService.changeWalletCoin(username, changingNum, reason, itemID);
            case TICKET:
                return walletService.changeWalletTicket(username, changingNum, reason, itemID);
            default:
                return false;
        }
    }

    @PostMapping(value = "/isVipAndIsCanDo")
    public VipOperationState isVipAndIsCanDo(@RequestParam String username, @RequestParam VipOperationType vipOperationType) {
        if (walletService.getById(username).getIsVip().equals((byte) 1)){
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
