package com.oriole.ocean.common.po.mysql;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.DateTimeException;
import java.util.Date;

@Data
@TableName("`note`")
public class NoteEntity implements java.io.Serializable {
    @TableId("note_id")
    private Integer noteID;

    private Integer noteType;
    private String content;
    private Integer likeNum;
    private Integer commentNum;
    private Integer readNum;
    private Date refreshDate;
    private Date buildDate;
    private String buildUsername;
    private Byte isAnon;
    private Byte isApproved;
    private Byte isAllowComment;

}
