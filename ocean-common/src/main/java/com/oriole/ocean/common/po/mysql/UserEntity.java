package com.oriole.ocean.common.po.mysql;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("user")
public class UserEntity implements java.io.Serializable {

    @TableId("username")
    private String username;
    private String nickname;
    private String password;
    private String email;
    private String phoneNum;
    private Byte isValid;
    private Date regDate;
    private String role;
    @TableField(value = "student_id")
    private String studentID;
    private String realname;
    private String avatar;
    private Integer levelGrade;
    @TableField(value = "cert_id")
    private Integer certID;

    @TableField(exist = false)  //数据库中是无此字段的，MP中要排除掉
    private WalletEntity wallet;
    @TableField(exist = false)  //数据库中是无此字段的，MP中要排除掉
    private UserExtraEntity userExtraEntity;
    @TableField(exist = false)  //数据库中是无此字段的，MP中要排除掉
    private UserCertificationEntity userCertificationEntity;

    @TableField(exist = false)
    private String level;

    public UserEntity() {
    }

    public UserEntity(String username, String nickname, String password, Byte isValid, String role) {
        this.username = username;
        this.nickname = nickname;
        this.password = password;
        this.isValid = isValid;
        this.regDate = new Date();
        this.role = role;

    }

    //限制信息获取时使用该构造器
    public UserEntity(String username, String nickname, String avatar, String level) {
        this.username = username;
        this.nickname = nickname;
        this.avatar = avatar;
        this.level = level;
    }

    public void setLevelGrade(Integer levelGrade) {
        this.levelGrade = levelGrade;
        this.level = "Lv." + levelGrade / 100;
    }
}
