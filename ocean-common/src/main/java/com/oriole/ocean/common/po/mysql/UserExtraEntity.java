package com.oriole.ocean.common.po.mysql;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("user_extra")
public class UserExtraEntity implements java.io.Serializable {

    @TableId("username")
    private String username;

    private String college;
    private String major;
    private Date birthday;
    private Integer sex;

    private String personalSignature;

    @TableField(value = "like_num")
    private Integer likeNumber;

    @TableField(value = "file_collected_num")
    private Integer fileCollectedNumber;

    public UserExtraEntity() {
    }
}
