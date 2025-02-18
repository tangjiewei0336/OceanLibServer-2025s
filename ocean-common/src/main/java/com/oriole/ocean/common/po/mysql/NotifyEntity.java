package com.oriole.ocean.common.po.mysql;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.oriole.ocean.common.enumerate.BehaviorType;
import com.oriole.ocean.common.enumerate.MainType;
import com.oriole.ocean.common.enumerate.NotifyAction;
import com.oriole.ocean.common.enumerate.NotifyType;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Data
@TableName("notify")
public class NotifyEntity implements java.io.Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String content;

    private NotifyType type;

    @TableField(value = "target_id")
    private Integer targetID;
    private MainType targetType;

    @TableField(value = "comment_id")
    private String commentID;

    private NotifyAction action;

    @TableField(value = "user_behavior_id")
    private String userBehaviorID;

    private String buildUsername;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date buildDate;

    public NotifyEntity() {
        this.buildDate = new Date();
    }

    public NotifyEntity(NotifyType type, String buildUsername) {
        this.type = type;
        this.buildUsername = buildUsername;
        this.buildDate = new Date();
    }

    public void setTargetIDAndType(Integer targetID,MainType targetType) {
        this.targetID = targetID;
        this.targetType = targetType;
    }
}
