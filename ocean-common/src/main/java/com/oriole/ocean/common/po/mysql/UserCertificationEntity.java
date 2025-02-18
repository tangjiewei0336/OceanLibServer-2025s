package com.oriole.ocean.common.po.mysql;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_certification")
public class UserCertificationEntity implements java.io.Serializable {

    @TableId("cert_id")
    private Integer certID;

    private String certName;
    private String certDesc;
    private String icon;

    public UserCertificationEntity() {
    }
}
