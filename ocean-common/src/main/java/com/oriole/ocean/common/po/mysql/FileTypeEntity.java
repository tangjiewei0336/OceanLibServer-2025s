package com.oriole.ocean.common.po.mysql;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("file_type")
public class FileTypeEntity implements java.io.Serializable {

    @TableId("type_id")
    private Integer typeID;
    private String typeName;

}
