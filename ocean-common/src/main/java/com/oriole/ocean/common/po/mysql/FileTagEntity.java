package com.oriole.ocean.common.po.mysql;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("file_tag")
public class FileTagEntity implements java.io.Serializable {

    @TableId("tag_id")
    private Integer tagID;

    private String tagName;
    private String addTime;
    private String addUsername;
    private Byte isBase;

    public FileTagEntity() {
    }

}
