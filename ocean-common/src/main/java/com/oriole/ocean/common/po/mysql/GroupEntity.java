package com.oriole.ocean.common.po.mysql;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("`file_index_group`")
public class GroupEntity implements java.io.Serializable {

    @TableId("group_id")
    private Integer groupID;

    private String groupName;
    private Byte isForever;
    private String showLocation;
    private String icon;
    private String extraDesc;

    private String includeTag;
    private String includeType;
    private String includeIndex;

    public GroupEntity() {
    }
}
