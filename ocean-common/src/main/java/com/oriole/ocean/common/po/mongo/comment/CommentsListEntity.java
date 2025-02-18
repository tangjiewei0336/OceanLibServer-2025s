package com.oriole.ocean.common.po.mongo.comment;
import com.oriole.ocean.common.enumerate.MainType;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class CommentsListEntity implements java.io.Serializable {
    private Integer bindID;
    private MainType type;
    private List<CommentEntity> comments;
    private Integer commentCount = null;

    public CommentsListEntity(Integer bindID, MainType type) {
        this.bindID = bindID;
        this.type = type;
        this.comments = new ArrayList<>();
        this.commentCount = 0;
    }
}
