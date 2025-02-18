package com.oriole.ocean.common.po.mongo.comment;

import lombok.Data;

import java.util.Date;

@Data
public abstract class AbstractComment implements java.io.Serializable {
    private String id;
    private String commentContent;
    private Integer likeNumber;
    private Integer dislikeNumber;
    private CommentStatusType commentStatus;
    private String buildDate;

    public AbstractComment(){
    }

    public AbstractComment(String id, String commentContent) {
        this.id = id;
        this.commentContent = commentContent;
        this.likeNumber = 0;
        this.dislikeNumber = 0;
        this.commentStatus = CommentStatusType.NORMAL;
        this.buildDate = String.valueOf(new Date().getTime());
    }

    //不可复写
    //控制评论可见状态
    public final String getCommentContent() {
        switch (this.commentStatus){
            case USER_DELETE:
                return "该评论已被用户自行删除";
            case USER_CLOSURE:
                return "该评论已被折叠";
            case ILLEGAL_CLOSURE:
                return "该评论因违反社区规范已被屏蔽";
            default:
                return commentContent;
        }
    }

    //确有必要时无视状态获取内容需要用这个方法
    public final String forceGetCommentContent() {
        return commentContent;
    }
}