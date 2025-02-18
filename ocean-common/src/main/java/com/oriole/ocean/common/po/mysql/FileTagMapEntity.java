package com.oriole.ocean.common.po.mysql;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("file_tag_map")
public class FileTagMapEntity implements java.io.Serializable {
    private Integer fileID;
    private Integer tagID;


    public FileTagMapEntity() {
    }

    public FileTagMapEntity(Integer fileID, Integer tagID) {
        this.fileID = fileID;
        this.tagID = tagID;
    }

    public Integer getFileID() {
        return fileID;
    }

    public void setFileID(Integer fileID) {
        this.fileID = fileID;
    }

    public Integer getTagID() {
        return tagID;
    }

    public void setTagID(Integer tagID) {
        this.tagID = tagID;
    }
}
