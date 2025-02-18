package com.oriole.ocean.common.po.mongo.comment;

public enum CommentStatusType {
    NORMAL("normal"),ILLEGAL_CLOSURE("I-CLOSE"),USER_CLOSURE("U-CLOSE"),USER_DELETE("U-DELETE");
    // 成员变量
    private final String statusName;

    // 构造方法
    private CommentStatusType(String statusName) {
        this.statusName = statusName;
    }

    // 覆盖方法
    @Override
    public String toString() {
        return this.statusName;
    }
}
