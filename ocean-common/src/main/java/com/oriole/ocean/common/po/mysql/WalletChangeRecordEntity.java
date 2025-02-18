package com.oriole.ocean.common.po.mysql;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("wallet_change_record")
public class WalletChangeRecordEntity implements java.io.Serializable {


    private String username;
    private String itemName;
    private Integer changeNum;
    private String reason;
    @TableField("pay_file_id")
    private Integer payFileID;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;

    public WalletChangeRecordEntity() {
    }

    public WalletChangeRecordEntity(String username, String itemName, Integer changeNum, String reason, Integer payFileID) {
        this.username = username;
        this.itemName = itemName;
        this.changeNum = changeNum;
        this.reason = reason;
        this.payFileID = payFileID;
        this.time=new Date();
    }

    public WalletChangeRecordEntity(String username, String itemName, Integer changeNum, String reason) {
        this.username = username;
        this.itemName = itemName;
        this.changeNum = changeNum;
        this.reason = reason;
        this.time=new Date();
    }
}
