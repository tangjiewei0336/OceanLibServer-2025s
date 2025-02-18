package com.oriole.ocean.common.po.mysql;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("file_upload_temp")
public class FileUploadTempEntity implements java.io.Serializable {

    @TableId(value = "upload_id", type = IdType.AUTO)
    private Integer uploadID;

    private String title;
    private Integer size;
    private String fileName;
    private String fileSuffix;
    private String previewPdfName;

    private String uploadUsername;
    private Date uploadDate;
    private String uploadApp;

    public FileUploadTempEntity() {
        this.uploadDate = new Date();
    }
}
