package com.oriole.ocean.common.po.mysql;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("file_search")
public class FileSearchEntity implements java.io.Serializable {

    @TableId(value = "file_id")
    private Integer fileID;

    private String content;

    private String keyword;

    public FileSearchEntity(Integer fileID, String content) {
        this.fileID = fileID;
        this.content = content;
    }
}
