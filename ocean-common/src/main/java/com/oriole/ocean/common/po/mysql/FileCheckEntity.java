package com.oriole.ocean.common.po.mysql;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("file_check")
public class FileCheckEntity implements java.io.Serializable {

    @TableId(value = "file_id")
    private Integer fileID;
    private Byte status;
    private String notice;
    @TableField("processing_time")
    private Date processingTime;
    private String rejectReason;
    @TableField("agree_reason")
    private String agreeReason;


    public FileCheckEntity() {
    }

    public FileCheckEntity(Integer fileID, Byte status, String rejectReason) {
        this.fileID = fileID;
        this.status = status;
        this.processingTime = new Date();
        this.rejectReason = rejectReason;
        this.agreeReason = agreeReason;
    }
}
