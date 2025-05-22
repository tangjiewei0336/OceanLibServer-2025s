package com.oriole.ocean.common.po.mysql;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("`note_type`")
public class NoteTypeEntity implements java.io.Serializable {
    @TableId("type_id")
    private Integer typeID;
    private String typeName;
    private String typeIcon;
    // private String cardColor;
}
