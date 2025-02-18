package com.oriole.ocean.common.po.mysql;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("user_notify")
public class UserNotifyEntity implements java.io.Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String username;
    private Byte isRead;

    @TableField(value = "notify_id")
    private Integer notifyID;
    private Date buildDate;

    @TableField(exist = false)
    private NotifyEntity notifyEntity;

    public UserNotifyEntity() {
        this.isRead = 0;
    }
}