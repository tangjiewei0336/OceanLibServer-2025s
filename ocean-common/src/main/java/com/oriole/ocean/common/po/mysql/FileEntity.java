package com.oriole.ocean.common.po.mysql;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Data
@TableName("file")
public class FileEntity implements java.io.Serializable {

    @TableId(value = "file_id", type = IdType.AUTO)
    private Integer fileID;

    private String title;
    private String abstractContent;
    private Integer size;
    private String previewPictureObjectName;
    private String fileType;
    private String uploadUsername;

    @JsonFormat(pattern = "yyyy-MM-dd HHmmss", timezone = "GMT+8")
    private Date uploadDate;
    private String realObjectName;
    private String previewPdfObjectName;
    private Byte paymentMethod;
    private Integer paymentAmount;
    private Byte isAllowAnon;
    private Byte isAllowVipfree;
    private Byte isAllowComment;
    private Double hideScore;

    @TableField(value = "folder_id")
    private Integer folderID;
    private Byte isApproved;
    private String indexString;
    @TableField(value = "type_id")
    private Integer typeID;


    @TableField(exist = false)  // 数据库中是无此字段的，MP中要排除掉
    private List<String> tagNames;

    @TableField(exist = false)  // 数据库中是无此字段的，MP中要排除掉
    private FileExtraEntity fileExtraEntity;

    @TableField(exist = false)  // 数据库中是无此字段的，MP中要排除掉
    private FileCheckEntity fileCheckEntity;

    public FileEntity() {
    }

    public FileEntity(String title, String abstractContent,
                      Integer size,String previewPictureObjectName,
                      String fileType, String uploadUsername,
                      String realObjectName, String previewPdfObjectName,
                      Byte paymentMethod, Integer paymentAmount,
                      Byte isAllowAnon, Byte isAllowVipfree, Integer folderID) {
        this.title = title;
        this.abstractContent = abstractContent;
        this.size = size;
        this.previewPictureObjectName = previewPictureObjectName;
        this.fileType = fileType;
        this.uploadUsername = uploadUsername;
        this.uploadDate=new Date();
        this.realObjectName = realObjectName;
        this.previewPdfObjectName = previewPdfObjectName;
        this.paymentMethod = paymentMethod;
        this.paymentAmount = paymentAmount;
        this.isAllowAnon=isAllowAnon;
        this.isAllowVipfree=isAllowVipfree;
        this.folderID = folderID;
        this.isApproved = 0;

    }
    public FileEntity(String title, String abstractContent, String uploadUsername, Integer folderID) {
        this.title = title;
        this.abstractContent = abstractContent;
        this.previewPictureObjectName = previewPictureObjectName;
        this.fileType = "folder";
        this.uploadUsername = uploadUsername;
        this.uploadDate = new Date();
        this.folderID = folderID;
        this.isApproved = 0;
    }

    public Date stringToDate(String dateString) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HHmm");
        return sdf.parse(dateString);  // 直接解析为 Date
    }

    public FileEntity(Integer fileID, String title,
                      String abstractContent,
                      Byte paymentMethod, Integer paymentAmount,
                      Byte isAllowAnon, Byte isAllowVipfree, Byte isAllowComment,
                      Integer folderID, String uploadDate) {
        this.fileID = fileID;
        this.title = title;
        this.abstractContent = abstractContent;
        this.paymentMethod = paymentMethod;
        this.paymentAmount = paymentAmount;
        this.isAllowAnon = isAllowAnon;
        this.isAllowVipfree = isAllowVipfree;
        this.isAllowComment = isAllowComment;
        this.folderID = folderID;
        try {
            this.uploadDate = this.stringToDate(uploadDate);
        } catch (ParseException e) {
            System.err.println("日期解析失败"  + uploadDate);
        }
    }
}
