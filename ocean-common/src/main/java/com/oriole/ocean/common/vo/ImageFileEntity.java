package com.oriole.ocean.common.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ImageFileEntity {
    private Integer uploadID;

    private Integer size;
    private String fileName;
    private String fileSuffix;

    private String uploadUsername;
    private Date uploadDate;
}
