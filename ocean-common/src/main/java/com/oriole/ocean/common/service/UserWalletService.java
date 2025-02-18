package com.oriole.ocean.common.service;

import com.oriole.ocean.common.enumerate.VipOperationState;
import com.oriole.ocean.common.enumerate.VipOperationType;

public interface UserWalletService {
    boolean changeWalletCoin(String username, Integer changingNum, String reason, Integer fileID);

    boolean changeWalletTicket(String username, Integer changingNum, String reason, Integer fileID);

    VipOperationState isVipAndIsCanDo(String username, VipOperationType vipOperationType);
}
