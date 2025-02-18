package com.oriole.ocean.common.po.mysql;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("file_extra")
public class FileExtraEntity implements java.io.Serializable {

    @TableId(value = "file_id")
    private Integer fileID;

    private Double score;
    private Integer ratersNum;
    private Integer readNum;
    private Integer likeNum;
    private Integer dislikeNum;
    private Integer downloadNum;
    private Integer collectionNum;
    private Integer commentNum;
    private Byte isProCert;
    private Byte isOfficial;
    private Byte isOriginal;
    private Byte isVipIncome;
    private String originalAuthor;
    private String copyrightNotice;

    public FileExtraEntity(Integer fileID, Byte isOriginal, String originalAuthor, String copyrightNotice) {
        this.fileID = fileID;
        this.isOriginal = isOriginal;
        this.originalAuthor = originalAuthor;
        this.copyrightNotice = copyrightNotice;
    }

    public FileExtraEntity() {
    }
}
