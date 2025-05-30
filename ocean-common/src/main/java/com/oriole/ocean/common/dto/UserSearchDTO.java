package com.oriole.ocean.common.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class UserSearchDTO {
    // 添加 setter 方法使 DTO 更完整
    private String username;
    private String nickname;
    private String realname;
    private String college;
    private String major;
    // 添加分页参数
    private Integer pageNum;
    private Integer pageSize;

}