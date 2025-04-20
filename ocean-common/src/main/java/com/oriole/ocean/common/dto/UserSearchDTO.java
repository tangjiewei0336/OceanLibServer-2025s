package com.oriole.ocean.common.dto;

public class UserSearchDTO {
    private String username;
    private String nickname;
    private String realname;
    private String college;
    private String major;

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }

    public String getRealname() {
        return realname;
    }

    public String getCollege() {
        return college;
    }

    public String getMajor() {
        return major;
    }

    // 添加 setter 方法使 DTO 更完整
    public void setUsername(String username) {
        this.username = username;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public void setMajor(String major) {
        this.major = major;
    }
}