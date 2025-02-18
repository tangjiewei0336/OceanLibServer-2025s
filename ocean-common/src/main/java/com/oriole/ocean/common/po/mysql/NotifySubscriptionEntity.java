package com.oriole.ocean.common.po.mysql;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.oriole.ocean.common.enumerate.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("notify_subscription")
public class NotifySubscriptionEntity implements java.io.Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "target_id")
    private String targetID;

    private NotifySubscriptionTargetType targetType;
    private NotifyAction action;
    private String username;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date buildDate;

    public NotifySubscriptionEntity() {
        this.buildDate = new Date();
    }

    public NotifySubscriptionEntity(String targetID, NotifySubscriptionTargetType targetType, NotifyAction action, String username) {
        this.targetID = targetID;
        this.targetType = targetType;
        this.action = action;
        this.username = username;
        this.buildDate = new Date();
    }
}
