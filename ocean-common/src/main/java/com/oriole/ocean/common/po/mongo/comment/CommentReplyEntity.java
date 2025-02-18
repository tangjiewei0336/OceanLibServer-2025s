package com.oriole.ocean.common.po.mongo.comment;

import lombok.Data;

@Data
public class CommentReplyEntity extends AbstractComment implements java.io.Serializable {
    private String replyBuildUsername;
    private String replyToCommentReplier;// 回复另一个回复时必填（被回复的回复者）
    private String replyToCommentReplyID;// 回复另一个回复时必填（被回复的回复ID）
    //private UserEntity replyBuildUserEntity = null;

    public CommentReplyEntity(String id, String replyBuildUsername,
                              String replyToCommentReplier,String replyToCommentReplyID, String commentContent) {
        super(id,commentContent);
        this.replyBuildUsername = replyBuildUsername;
        this.replyToCommentReplier = replyToCommentReplier;
        this.replyToCommentReplyID = replyToCommentReplyID;
    }
}
