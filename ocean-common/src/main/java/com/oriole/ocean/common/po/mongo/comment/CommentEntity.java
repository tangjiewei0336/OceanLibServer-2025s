package com.oriole.ocean.common.po.mongo.comment;

import lombok.Data;

import java.util.ArrayList;

@Data
public class CommentEntity extends AbstractComment implements java.io.Serializable {
    private String commentBuildUsername;
    private Integer hotValue;
    private Integer replyCount;
    //private UserEntity commentBuildUserEntity = null;
    private ArrayList<CommentReplyEntity> replyCommentList = null;

    public CommentEntity(String id, String commentBuildUsername, String commentContent) {
        super(id,commentContent);
        this.commentBuildUsername = commentBuildUsername;
        this.replyCommentList = new ArrayList<>();
        this.replyCount = 0;
    }
}
