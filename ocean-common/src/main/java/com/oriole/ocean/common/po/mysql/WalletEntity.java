package com.oriole.ocean.common.po.mysql;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("wallet")
public class WalletEntity implements java.io.Serializable {

    @TableId(value = "username")
    private String username;
    private Integer coin;
    private Integer exp;
    private Integer ticket;
    private Byte isVip;
    private Date vipValidDate;

}
